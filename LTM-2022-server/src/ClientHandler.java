import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ea.async.Async;

public class ClientHandler implements Runnable {

    private static ArrayList<ClientHandler> clientList = new ArrayList<>();
    private final String clientName;
    private BufferedReader in = null;
    private BufferedWriter out = null;
    private Socket socket = null;
    private static final String searchURL = "https://www.imdb.com/find?q=";
    private static final String getFilmURL = "https://www.imdb.com";
    //    Object lock = new Object();
    private static int index = 0;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.clientName = "Client[" + ++index + "]";
        clientList.add(this);
    }

    public void closeConnect() {
        try {
            clientList.remove(this);
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public Document getDoc(String host, String searchValue) {
        try {
            return Jsoup.connect(host + searchValue)
                    .method(Connection.Method.GET)
                    .execute().parse();
        } catch (IOException e) {
            e.printStackTrace();
            closeConnect();
        }
        return null;
    }

    public void searchFilm() {
        while(socket.isConnected()) {
            JSONObject json = new JSONObject();
            Elements searchElements;
            Elements getElements;
            String link;
            Document searchDoc;
            Document getFilmDoc;
            try {
                String searchValue = in.readLine();
                String[] tmp = searchValue.split(" ");
                searchValue = "";
                searchValue = String.join("+", tmp);
                System.out.println(clientName + ": " + searchValue);

                while (true) {
                    searchDoc = getDoc(searchURL, searchValue);
                    searchElements= searchDoc.body().getElementsByAttributeValue("class", "ipc-metadata-list-summary-item ipc-metadata-list-summary-item--click find-result-item find-title-result");
                    if (searchElements.size() == 0)
                        continue;
                    break;
                }

                for(Element searchElement : searchElements){
                    link = searchElement.getElementsByTag("a").get(0).attr("href");
                    while(true){
                        getFilmDoc = getDoc(getFilmURL, link);
                        getElements = getFilmDoc.body().getElementsByAttributeValue("class", "ipc-page-section ipc-page-section--baseAlt ipc-page-section--tp-none ipc-page-section--bp-xs sc-2a827f80-1 gvCXlM");
                        if(getElements.size() == 0)
                            continue;
                        break;
                    }
                    String imdb = "";
                    String filmPoster = "";
                    String filmTrailer = "";
                    String filmGern = "";
                    String filmDescription = "";
                    String filmDirector = "";

                    for (Element getElement : getElements){
                        imdb = getElement.getElementsByAttributeValue("class", "sc-7ab21ed2-1 jGRxWM").get(0).text();
                        filmPoster = getElement.getElementsByAttributeValue("class", "ipc-image").get(0)
                                .attr("srcset")
                                .split(" ")[4];
                        String filmTrailerLink = getElement.getElementsByAttributeValue("data-testid", "video-player-slate-overlay")
                                        .attr("href");
                        if(filmTrailerLink.equals(""))
                            filmTrailer = "None";
                        else
                            filmTrailer = getFilmURL + filmTrailerLink;
                        Elements gernSpanTag = getElement.getElementsByAttributeValue("class", "ipc-chip-list__scroller")
                                .get(0).getElementsByTag("span");

                        ArrayList<String> gernList = new ArrayList<>();
                        for(Element gern : gernSpanTag){
                            gernList.add(gern.text());
                        }
                        filmGern = String.join(", ", gernList);

                        filmDescription = getElement.getElementsByAttributeValue("data-testid", "plot-xl").get(0).text();

                        Elements directorATag = getElement.getElementsByAttributeValue("class", "ipc-inline-list ipc-inline-list--show-dividers ipc-inline-list--inline ipc-metadata-list-item__list-content baseAlt")
                                        .get(0).getElementsByTag("a");

                        ArrayList<String> directorList = new ArrayList<>();
                        for(Element director : directorATag){
                            directorList.add(director.text());
                        }
                        filmDirector = String.join(", ", directorList);
//                        System.out.println(filmDirector);
                    }
                    String filmName = searchElement.getElementsByTag("a").get(0).text();
                    Elements spanTag = searchElement.getElementsByTag("span");
                    String filmYear = spanTag.get(0).text();
                    String actors = spanTag.get(1).text();

                    json.put("filmName", filmName);
                    json.put("filmYear", filmYear);
                    json.put("actors", actors);
                    json.put("imdb", imdb);
                    json.put("filmPoster", filmPoster);
                    json.put("filmTrailer", filmTrailer);
                    json.put("filmGern", filmGern);
                    json.put("filmDescription", filmDescription);
                    json.put("filmDirector", filmDirector);
                    out.write(json.toString());
                    out.newLine();
                }
                out.write("done");
                out.newLine();
                out.flush();
            } catch (Exception error) {
//                error.printStackTrace();
                closeConnect();
                break;
            }
        }
    }

    @Override
    public void run() {
        searchFilm();
    }
}
