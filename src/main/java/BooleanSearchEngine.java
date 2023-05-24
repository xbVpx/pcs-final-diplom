import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> indexing; //мапа индексации поиска

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        // прочтите тут все pdf и сохраните нужные данные,
        // тк во время поиска сервер не должен уже читать файлы

        List<File> files; //создаем список файлов директории pdfs
        try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(pdfsDir)))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        indexing = new HashMap<>();

        for (File file : files) { //итерируем полученный список файлов
            var doc = new PdfDocument(new PdfReader(file)); //создаем объект пдф-документа

            for (int i = 1; i < doc.getNumberOfPages() + 1; i++) {
                var pdfPage = doc.getPage(i);  //получаем объект одной страницы документа
                var text = PdfTextExtractor.getTextFromPage(pdfPage); //получаем текст со страницы
                var words = text.split("\\P{IsAlphabetic}+"); //разбиваем полученный текст на слова
                Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота

                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : freqs.entrySet()) { //перебираем мапу слово-частота
                    List<PageEntry> pageEntries;
                    String key = entry.getKey(); //получаем ключ
                    Integer value = entry.getValue(); //получаем значение
                    if (indexing.containsKey(key)) {
                        pageEntries = indexing.get(key);
                    } else {
                        pageEntries = new ArrayList<>();
                    }
                    pageEntries.add(new PageEntry(file.getName(), i, value));
                    indexing.put(key, pageEntries); //заполняем индекс
                }

                for (var entry : indexing.entrySet()) {
                    Collections.sort(entry.getValue()); //сортируем список поиска
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        // реализация поиска по слову
        List<PageEntry> list = indexing.get(word.toLowerCase());

/* полный вариант ретурна который ниже
        if (list != null) {
            return list;
        }
        return Collections.emptyList();
*/
        return Objects.requireNonNullElse(list, Collections.emptyList());
    }
}