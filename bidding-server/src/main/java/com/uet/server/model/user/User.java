package com.uet.server.model.user;

import java.io.Serializable;

public class User implements Serializable {
   private String id;
   private String username;
   private String role;

   public User() {

   }
   public User(String id, String username,String role){
       this.id = id;
       this.username=username;
       this.role=role;
   }
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
}