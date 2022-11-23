package com.ltm2022client.application;

import com.ltm2022client.models.Film;
import com.ltm2022client.models.Review;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;

public class SearchFilmController implements Initializable {
    private static final ObservableList<Film> searchList = FXCollections.observableArrayList();
    private static final ObservableList<Review> reviewFilmList = FXCollections.observableArrayList();
    private static final String errorCharacter = "'\"@#^&*\\{}[]|`~<>/;()+";

    @FXML
    private TextField searchTxtField;

    @FXML
    private Button searchBtn;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private AnchorPane filmField;

    private static Stage primaryStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Handler();
    }

    private boolean checkRegex(String value, String regex){
        for(int i=0; i<value.length(); i++){
            for(int j=0; j<regex.length(); j++)
                if(value.charAt(i) == regex.charAt(j))
                    return true;
        }
        return false;
    }

    public void Handler() {
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String rawValue = searchTxtField.getText().trim();
                if(checkRegex(rawValue, errorCharacter) || rawValue.length() == 0){
                    errorAlert("Lỗi", "Bạn đã nhập kí tự đặc biêt hoặc không nhập gì, vui lòng kiểm tra lại.");
                }
                else{
                    try {
                        if (searchList.size() != 0){
                            searchList.clear();
                        }

                        if (reviewFilmList.size() != 0){
                            reviewFilmList.clear();
                        }

                        String seachValue = Vigenere.Decode(rawValue, MainController.key);
                        MainController.out.write(seachValue);
                        MainController.out.newLine();
                        MainController.out.flush();
                        String line = Vigenere.Encode(MainController.in.readLine(), MainController.key);
                        JSONObject json = new JSONObject(line);
                        Film film = new Film();
                        film.setActor(json.getString("actors"));
                        film.setPoster(json.getString("filmPoster"));
                        film.setImdb(json.getString("imdb"));
                        film.setDescription(json.getString("filmDescription"));
                        film.setTrailer(json.getString("filmTrailer"));
                        film.setDirector(json.getString("filmDirector"));
                        film.setName(json.getString("filmName"));
                        film.setYear(json.getString("filmYear"));
                        film.setGern(json.getString("filmGern"));
                        JSONArray reviewList = json.getJSONArray("reviewList");

                        for (Object review : reviewList){
                            Review rv = new Review();
                            JSONObject reviewToJson = new JSONObject(review.toString());
                            rv.setTitle(reviewToJson.getString("title"));
                            rv.setContent(reviewToJson.getString("content"));
                            rv.setUserName(reviewToJson.getString("username"));
                            reviewFilmList.add(rv);
                        }
                        searchList.add(film);

                        loadFilmFromSearchList();
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                }
            }
        });
    }

    private void errorAlert(String title, String Message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        DialogPane root = alert.getDialogPane();
//        root.getStylesheets().add((getClass().getResource("main.css")).toExternalForm());
        root.getScene().setFill(Color.TRANSPARENT);
        Stage dialogStage = (Stage) root.getScene().getWindow();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        alert.setContentText(Message);
        alert.setHeaderText(null);
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okBtn);
        alert.show();
    }

    public void loadFilmFromSearchList() throws IOException {
        if(searchList.size() > 0){
            for (Film film : searchList) {
                FXMLLoader filmLoader = new FXMLLoader();
                filmLoader.setLocation(getClass().getResource("film-item.fxml"));
                AnchorPane filmBox = filmLoader.load();
                FilmItemCotroller filmItemCotroller = filmLoader.getController();
                filmItemCotroller.setValue(film, reviewFilmList);
                mainPane.getChildren().clear();
                mainPane.getChildren().add(filmBox);
                AnchorPane.setTopAnchor(filmBox,0.0);
                AnchorPane.setBottomAnchor(filmBox,0.0);
                AnchorPane.setLeftAnchor(filmBox,0.0);
                AnchorPane.setRightAnchor(filmBox,0.0);
            }
        }
    }
}
