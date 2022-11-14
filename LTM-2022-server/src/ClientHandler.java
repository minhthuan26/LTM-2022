import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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

    public String randomKey(){
        int keyLength = (int) ( 11 * Math.random() + 10);
        String alphabet = "abcdefghijklmnopqrstuvxyz";

        StringBuilder key = new StringBuilder(keyLength);
        for(int i=0; i<keyLength; i++){
            int index = (int)(alphabet.length() * Math.random());

            // add Character one by one in end of sb
            key.append(alphabet.charAt(index));
        }
        return key.toString();
    }

    public void searchFilm() {
        String key = null;
        if(socket.isConnected()){
            try {
                key = randomKey();
                out.write(key);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while(socket.isConnected()) {
            JSONObject json = new JSONObject();
            Element searchElement;
            Element getElement;
            String link;
            Document searchDoc;
            Document getFilmDoc;
            String imdb = "None";
            String filmPoster = "None";
            String filmTrailer = "None";
            String filmGern = "None";
            String filmDescription = "None";
            String filmDirector = "None";
            JSONArray reviewList = new JSONArray();
            try {
                String searchValue = Vigenere.Encode(in.readLine(), key);
                String[] tmp = searchValue.split(" ");
                searchValue = "";
                searchValue = String.join("+", tmp);
                searchValue += "&ref_=nv_sr_sm";
                System.out.println(clientName + ": " + searchValue);

                while (true) {
                    searchDoc = getDoc(searchURL, searchValue);
                    Elements checkSearch = searchDoc.body().getElementsByAttributeValue("class", "ipc-metadata-list-summary-item ipc-metadata-list-summary-item--click find-result-item find-title-result");
                    if (checkSearch.size() == 0)
                        continue;
                    searchElement = checkSearch.get(0);
                    break;
                }
                //
                link = searchElement.getElementsByTag("a").get(0).attr("href");
                while(true){
                    getFilmDoc = getDoc(getFilmURL, link);
                    getElement = getFilmDoc.body().getElementsByAttributeValueContaining("class", "ipc-page-section").get(0);
                    if(getElement == null)
                        continue;
                    break;
                }
                //
                Elements checkImdb = getElement.getElementsByAttributeValue("class", "sc-7ab21ed2-1 jGRxWM");
                if(checkImdb.size() != 0)
                    imdb = checkImdb.get(0).text();
                Elements checkPoster = getElement.getElementsByAttributeValue("class", "ipc-image");
                if(checkPoster.size() != 0)
                    filmPoster = checkPoster.get(0)
                            .attr("srcset")
                            .split(" ")[4];
                String filmTrailerLink = getElement.getElementsByAttributeValue("data-testid", "video-player-slate-overlay")
                        .attr("href");
                if(!filmTrailerLink.equals(""))
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
                //
                Elements reviewLinks = getElement.getElementsByAttributeValueContaining("class", "isReview");
                if(reviewLinks.size() != 0){
                    String reviewUrl = reviewLinks.get(0).attr("href");
                    Document reviewDoc = getDoc(getFilmURL, reviewUrl);
                    Elements titles = reviewDoc.select("#main > section > div.lister > div.lister-list > div > div.review-container > div.lister-item-content > a");
                    Elements usernames = reviewDoc.select("#main > section > div.lister > div.lister-list > div > div.review-container > div.lister-item-content > div.display-name-date > span.display-name-link > a");
                    Elements contents = reviewDoc.select("#main > section > div.lister > div.lister-list > div > div.review-container > div.lister-item-content > div.content > div");

                    for(int i=0; i<titles.size(); i++){
                        JSONObject review = new JSONObject();
                        review.put("title", titles.get(i).text());
                        review.put("username", usernames.get(i).text());
                        review.put("content", contents.get(i).text());
                        reviewList.put(review);
                    }
                }
                //
                String filmName = searchElement.getElementsByTag("a").get(0).text();
                Elements yearUlTag = getElement.getElementsByAttributeValue("data-testid", "hero-title-block__metadata");
                String filmYear = yearUlTag.get(0).getElementsByTag("a").get(0).text();
                String actors = "";
                Element actorsUlTag = getElement.getElementsByAttributeValue("class", "ipc-inline-list ipc-inline-list--show-dividers ipc-inline-list--inline ipc-metadata-list-item__list-content baseAlt").get(2);
                Elements actorsName = actorsUlTag.getElementsByTag("a");
                String[] actorsNameArray = new String[actorsName.size()];
                for(int i=0; i<actorsName.size(); i++){
                    actorsNameArray[i] = actorsName.get(i).text();
                }
                actors = String.join(", ", actorsNameArray);

                json.put("filmName", filmName);
                json.put("filmYear", filmYear);
                json.put("actors", actors);
                json.put("imdb", imdb);
                json.put("filmPoster", filmPoster);
                json.put("filmTrailer", filmTrailer);
                json.put("filmGern", filmGern);
                json.put("filmDescription", filmDescription);
                json.put("filmDirector", filmDirector);
                json.put("reviewList", reviewList);
                out.write(Vigenere.Decode(json.toString(), key));
                out.newLine();
                //
//                out.write("done");
//                out.newLine();
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
