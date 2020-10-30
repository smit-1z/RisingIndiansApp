package com.example.risingindians;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class EventPost extends EventPostId{

    public String user_id,image_url,desc;
    public Date timestamp;

    public EventPost(){

    }
    public EventPost(String user_id, String image_url, String desc, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.timestamp = timestamp;
    }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


}
