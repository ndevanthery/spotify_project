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


    public String getMusicName() {
        return musicName;
    }


    public String getArtiste() {
        return artiste;
    }


    public String getAlbum() {
        return album;
    }

}
