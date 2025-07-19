package com.mobileApp.finTra.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "first_name", nullable = false)
    private String first_name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "last_name", nullable = false)
    private String last_name;

    @Column(name = "middle_name")
    private String middle_name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "hash_password", nullable = false)
    private String password;

    @Column(name = "account_type")
    private String account_type;

    @Column(name = "phone")
    private String phone;

    @Column(name = "country")
    private String country;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "createdAt", insertable = false, updatable = false)
    private java.sql.Timestamp createdAt;

    @Column(name = "updatedAt", insertable = false, updatable = false)
    private java.sql.Timestamp updatedAt;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<TransactionModel> transactions = new ArrayList<>();


    @Column(name = "wallet_balance", precision = 15, scale = 2, nullable = false)
    private int balance;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int walletBalance) {
        this.balance = walletBalance;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", middle_name='" + middle_name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", account_type='" + account_type + '\'' +
                ", phone='" + phone + '\'' +
                ", country='" + country + '\'' +
                ", dob='" + dob + '\'' +
                '}';
    }
}
