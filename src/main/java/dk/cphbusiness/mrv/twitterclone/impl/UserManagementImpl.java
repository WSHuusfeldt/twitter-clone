package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.UserManagement;
import dk.cphbusiness.mrv.twitterclone.dto.UserCreation;
import dk.cphbusiness.mrv.twitterclone.dto.UserOverview;
import dk.cphbusiness.mrv.twitterclone.dto.UserUpdate;
import dk.cphbusiness.mrv.twitterclone.util.Time;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserManagementImpl implements UserManagement {

    private Jedis jedis;

    public UserManagementImpl(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public boolean createUser(UserCreation userCreation) {
        boolean exists = jedis.hexists("user#"+userCreation.username, "username");
        if(exists) return false;

        Map<String, String> user = Map.of(
                "username", userCreation.username,
                "firstname", userCreation.firstname,
                "lastname", userCreation.lastname,
                "passwordHash", userCreation.passwordHash,
                "birthday", userCreation.birthday
        );

        jedis.hmset("user#"+userCreation.username, user);
        return true;
    }

    @Override
    public UserOverview getUserOverview(String username) {

        boolean exists = jedis.hexists("user#" + username, "username");
        if(!exists) return null;

        Map<String, String> user = jedis.hgetAll("user#" + username);
        return new UserOverview(username, user.get("firstname"),
                user.get("lastname"),
                jedis.smembers("followers#" + username).size(),
                jedis.smembers("following#" + username).size());
    }

    @Override
    public boolean updateUser(UserUpdate userUpdate) {
        boolean exists = jedis.hexists("user#" + userUpdate.username, "username");
        if(!exists) return false;
        if(userUpdate.firstname != null) jedis.hset("user#" + userUpdate.username, "firstname", userUpdate.firstname);
        if(userUpdate.lastname != null) jedis.hset("user#" + userUpdate.username, "lastname", userUpdate.lastname);
        if(userUpdate.birthday != null) jedis.hset("user#" + userUpdate.username, "birthday", userUpdate.birthday);

        return true;
    }

    @Override
    public boolean followUser(String username, String usernameToFollow) {
        boolean toFollow = jedis.hexists("user#"+usernameToFollow, "username");
        boolean user = jedis.hexists("user#"+username, "username");
        if(!user || !toFollow) return false;

        jedis.sadd("following#" + username, usernameToFollow);
        jedis.sadd("followers#" + usernameToFollow, username);

        return true;
    }

    @Override
    public boolean unfollowUser(String username, String usernameToUnfollow) {
        boolean follower = jedis.hexists("user#" + usernameToUnfollow, "username");
        boolean user = jedis.hexists("user#" + username, "username");
        if(!user || !follower) return false;

        jedis.srem("following#" + username, usernameToUnfollow);
        jedis.srem("followers#" + usernameToUnfollow, username);
        return true;
    }

    @Override
    public Set<String> getFollowedUsers(String username) {
        boolean exists = jedis.hexists("user#" + username, "username");
        if(!exists) return null;

        Set<String> following = jedis.smembers("following#" + username);
        return following;
    }

    @Override
    public Set<String> getUsersFollowing(String username) {
        boolean exists = jedis.hexists("user#" + username, "username");
        if(!exists) return null;

        Set<String> followers = jedis.smembers("followers#" + username);
        return followers;
    }

}
