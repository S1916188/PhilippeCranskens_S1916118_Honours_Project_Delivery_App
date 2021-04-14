package com.example.pizzadeliveryapp;

public class User
{
    //Attributes
    private int UserId;
    private String Username;
    private String Password;
    private String EmailAddress;

    //Getters and setters
    public String getUsername()
{
    return Username;
}
    public void setUsername(String username)
    {
        Username = username;
    }

    public String getPassword()
    {
        return Password;
    }
    public void setPassword(String password)
    {
        Password = password;
    }

    public int getUserId()
    {
        return UserId;
    }
    public void setUserId(int userId)
    {
        UserId = userId;
    }

    public String getEmailAddress()
    {
        return EmailAddress;
    }
    public void setEmailAddress(String emailAddress)
    {
        EmailAddress = emailAddress;
    }


    //constructors
    public User()
    {
        UserId = 0;
    }

    public User(int userId, String username, String password, String emailAddress)
    {
        UserId = userId;
        Username = username;
        Password = password;
        EmailAddress = emailAddress;
    }
}
