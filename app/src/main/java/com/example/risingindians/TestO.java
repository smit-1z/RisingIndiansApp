package com.example.risingindians;

import com.akshaykale.swipetimeline.TimelineObject;

public class TestO implements TimelineObject {

    public TestO(long timeline, String name, String url) {
        this.timeline = timeline;
        this.name = name;
        this.url = url;
    }

    long timeline;
    String name, url;

    @Override
    public long getTimestamp() {
        return timeline;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getImageUrl() {
        return url;
    }
}
