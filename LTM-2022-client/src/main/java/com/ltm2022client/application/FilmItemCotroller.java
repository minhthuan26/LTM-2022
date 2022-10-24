package com.ltm2022client.application;

import com.ltm2022client.models.Film;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class FilmItemCotroller implements Initializable {

    @FXML
    private Label actorLbl;

    @FXML
    private Label directorLbl;

    @FXML
    private Label gernLbl;

    @FXML
    private Label imdbLbl;

    @FXML
    private Label nameLbl;

    @FXML
    private ImageView posterImg;

    @FXML
    private Button trailerBtn;

    @FXML
    private TextArea desTxf;

    @FXML
    private Label yearLbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setValue(Film film) {
        yearLbl.setText(film.getYear());
//        trailerBtn.;
        if(film.getTrailer().equals("None"))
            trailerBtn.setDisable(true);
        Image img = new Image(film.getPoster());
        posterImg.setImage(img);
        nameLbl.setText(film.getName());
        imdbLbl.setText(film.getImdb());
        gernLbl.setText(film.getGern());
        desTxf.setText(film.getDescription());
        directorLbl.setText(film.getDirector());
        actorLbl.setText(film.getActor());
    }
}
