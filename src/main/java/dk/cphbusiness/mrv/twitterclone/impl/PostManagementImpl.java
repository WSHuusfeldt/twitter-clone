package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.PostManagement;
import dk.cphbusiness.mrv.twitterclone.dto.Post;
import dk.cphbusiness.mrv.twitterclone.util.Time;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PostManagementImpl implements PostManagement {
    private Jedis jedis;
    private Time time;

    public PostManagementImpl(Jedis jedis, Time time) {
        this.jedis = jedis;
        this.time = time;
    }

    @Override
    public boolean createPost(String username, String message) {
        boolean user = jedis.hexists("user#" + username, "username");
        if(!user) return false;
        long ts = time.getCurrentTimeMillis();
        jedis.hset("posts#" + username, "" + ts, message);

        return true;
    }

    @Override
    public List<Post> getPosts(String username) {
        Map<String, String> posts = jedis.hgetAll("posts#" + username);
        List<Post> listPosts = new ArrayList<>();
        for(String ts : posts.keySet()) {
            listPosts.add(new Post(Long.parseLong(ts), posts.get(ts)));
        }
        return listPosts;
    }

    @Override
    public List<Post> getPostsBetween(String username, long timeFrom, long timeTo) {
        Map<String, String> posts = jedis.hgetAll("posts#" + username);
        List<Post> listPosts = new ArrayList<>();
        for(String ts : posts.keySet()) {
            if(Long.parseLong(ts) <= timeTo && Long.parseLong(ts) >= timeFrom)
                listPosts.add(new Post(Long.parseLong(ts), posts.get(ts)));
        }
        return listPosts;    }
}
