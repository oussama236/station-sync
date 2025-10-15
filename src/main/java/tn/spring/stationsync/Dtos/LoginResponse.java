package tn.spring.stationsync.Dtos;

public class LoginResponse {

    private String token;
    private int openNotificationCount;
    private java.util.List<tn.spring.stationsync.Entities.Notification> recentOpenNotifications;

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse(String token, int openNotificationCount,
                         java.util.List<tn.spring.stationsync.Entities.Notification> recentOpenNotifications) {
        this.token = token;
        this.openNotificationCount = openNotificationCount;
        this.recentOpenNotifications = recentOpenNotifications;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getOpenNotificationCount() {
        return openNotificationCount;
    }

    public void setOpenNotificationCount(int openNotificationCount) {
        this.openNotificationCount = openNotificationCount;
    }

    public java.util.List<tn.spring.stationsync.Entities.Notification> getRecentOpenNotifications() {
        return recentOpenNotifications;
    }

    public void setRecentOpenNotifications(java.util.List<tn.spring.stationsync.Entities.Notification> recentOpenNotifications) {
        this.recentOpenNotifications = recentOpenNotifications;
    }
}
