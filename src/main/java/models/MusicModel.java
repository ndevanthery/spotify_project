package models;

public class MusicModel
{
    private String url;
    private String musicName;
    private String artiste;
    private String album;


    public MusicModel(String url,String musicName,String artiste, String album)
    {
        this.url = url;
        this.musicName=musicName;
        this.album=album;
        this.artiste=artiste;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getArtiste() {
        return artiste;
    }

    public void setArtiste(String artiste) {
        this.artiste = artiste;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
