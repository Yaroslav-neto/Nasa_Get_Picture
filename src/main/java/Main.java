import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.config.RequestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    // Наша ссылка на API NASA
    public static final String URI = "https://api.nasa.gov/planetary/apod?api_key=Здесь ВАШ КЛЮЧ";
//    public static final String URI = "https://api.nasa.gov/planetary/apod?api_key=Здесь ВАШ КЛЮЧ&date=2025-03-09"; // вызов картинки за указанную дату
    // Объект для преобразования ответа в NasaObject
    public static final ObjectMapper mapper = new ObjectMapper();

    // Логгер для логирования информации и ошибок
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Путь к директории, в которой необходимо сохранять изображения
        String directoryPath = "D:\\PROJECT MAVEN\\Nasa\\PictureAdd"; // замените на актуальный путь

        // Создание объекта директории
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // Создание директории, если она не существует
        }

        // Настраиваем HTTP клиент
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build()) {

            // Отправляем запрос и получаем ответ
            try (CloseableHttpResponse response = httpClient.execute(new HttpGet(URI))) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    NasaObject nasaObject = mapper.readValue(response.getEntity().getContent(), NasaObject.class);
                    logger.info("Получены данные от NASA: {}", nasaObject);

                    // Запрос изображения
                    try (CloseableHttpResponse pictureResponse = httpClient.execute(new HttpGet(nasaObject.getUrl()))) {
                        if (pictureResponse.getStatusLine().getStatusCode() == 200) {
                            // Формируем название для файла и полный путь
                            String[] arr = nasaObject.getUrl().split("/");
                            String fileName = "PictureToday.jpg"; // запись заданного имени файла
//                            String fileName = arr[arr.length - 1]; // запись оригинального Название файла
                            String fullPath = directoryPath + File.separator + fileName; // Полный путь

                            HttpEntity entity = pictureResponse.getEntity();

                            // Сохраняем в файл
                            try (FileOutputStream fos = new FileOutputStream(fullPath)) {
                                entity.writeTo(fos);
                            }
                            logger.info("Изображение сохранено в: {}", fullPath);
                        } else {
                            logger.error("Ошибка получения изображения: {}", pictureResponse.getStatusLine());
                        }
                    }
                } else {
                    logger.error("Ошибка получения данных от NASA: {}", response.getStatusLine());
                }
            }

        } catch (IOException e) {
            logger.error("Произошла ошибка при выполнении запроса", e);
        }
    }
}
