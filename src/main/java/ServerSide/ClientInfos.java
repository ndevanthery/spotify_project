package ServerSide;

import models.MusicModel;

import java.util.List;

public class ClientInfos
{
    private int id;
    private String ip;
    private List<MusicModel> listOfsongs;
    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ClientInfos(int id, String ip, int port, List<MusicModel> listOfsongs)
    {
        this.id = id;
        this.ip = ip;
        this.listOfsongs = listOfsongs;
        this.port = port;




    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<MusicModel> getListOfSongs() {
        return listOfsongs;
    }

    public void setListOfSongs(List<MusicModel> listOfsongs) {
        this.listOfsongs = listOfsongs;
    }

    @Override
    public String toString() {
        return "ClientInfos{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", listOfsongs=" + listOfsongs +
                '}';
    }
}
