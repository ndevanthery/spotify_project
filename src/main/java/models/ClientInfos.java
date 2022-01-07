package models;

import models.MusicModel;

import java.util.List;

public class ClientInfos
{
    private int id;
    private String username;
    private String ip;
    private List<MusicModel> listOfsongs;



    public ClientInfos(int id, String username, String ip, List<MusicModel> listOfsongs)
    {
        this.id = id;
        this.username = username;
        this.ip = ip;
        this.listOfsongs = listOfsongs;
    }

    public int getId() {
        return id;
    }


    public String getIp() {
        return ip;
    }


    public List<MusicModel> getListOfSongs() {
        return listOfsongs;
    }


    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "ClientInfos : " +
                "\tid:" + id +
                "\tusername:" + username+
                "\tip=:" + ip;

    }
}
