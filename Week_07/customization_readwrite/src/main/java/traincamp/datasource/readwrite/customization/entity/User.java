package traincamp.datasource.readwrite.customization.entity;

import java.util.Date;

public class User {
    private Integer id;

    private String nickname;

    private String loginName;

    private String password;

    private String mobile;

    private Date createdTime;

    private Date updatedTime;

    private Byte isDelete;

    public User(Integer id, String nickname, String loginName, String password, String mobile, Date createdTime, Date updatedTime, Byte isDelete) {
        this.id = id;
        this.nickname = nickname;
        this.loginName = loginName;
        this.password = password;
        this.mobile = mobile;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.isDelete = isDelete;
    }

    public User() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName == null ? null : loginName.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }
}