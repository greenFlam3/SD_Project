package com.googol;

import com.googol.IndexStorageBarrel;
import com.googol.URLQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.StringTokenizer;

public class Downloader {
    
    public static void processURL(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Indexar palavras da página
            StringTokenizer tokenizer = new StringTokenizer(doc.text());
            while (tokenizer.hasMoreTokens()) {
                IndexStorageBarrel.addToIndex(tokenizer.nextToken(), url);
            }

            // Extrair e adicionar links à fila
            Elements links = doc.select("a[href]");
            links.forEach(link -> URLQueue.addURL(link.absUrl("href")));

            System.out.println("Indexado: " + url);
        } catch (IOException e) {
            System.err.println("Erro ao acessar URL: " + url);
        }
    }

    public static void main(String[] args) {
        // Adiciona URLs iniciais para teste
        URLQueue.addURL("https://example.com");

        while (!URLQueue.isEmpty()) {
            String url = URLQueue.getNextURL();
            if (url != null) processURL(url);
        }

        // Teste de pesquisa
        System.out.println("Páginas com 'example': " + IndexStorageBarrel.search("example"));
    }
}
