package com.uet.common.model.user;

import java.io.Serializable;
import com.uet.common.model.user.Role;


public class User implements Serializable {
   private String id;
   private String username;
   private Role role;
   private Double balance;

   public User() {

   }
   public User(String id, String username,Role role,Double balance){
       this.id = id;
       this.username=username;
       this.role=role;
       this.balance=balance;
   }
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(Role role){this.role=role; }
    public Role getRole() { return this.role; }
    public String getUsername() { return this.username; }
    public Double getBalance() { return this.balance; }
    public String getId() { return this.id; }
}