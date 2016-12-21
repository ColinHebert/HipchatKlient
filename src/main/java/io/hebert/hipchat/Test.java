package io.hebert.hipchat;

import io.hebert.hipchat.api.User;
import io.hebert.hipchat.api.UserList;
import io.hebert.hipchat.api.HipchatUserAPI;
import io.hebert.hipchat.helper.HipchatUserHelper;

/**
 * Created by Makkhdyn on 3/12/16.
 */
public class Test {
    public static void main(String[] args) {
        HipchatClient hc = new HipchatClient("token");
        hc.checkAuthentication();
        UserList allUsers = HipchatUserAPI.getAllUsers(hc, 0, 1);
        UserList nextPage = HipchatUserHelper.getNextPage(allUsers, hc);
        System.out.println(allUsers);
        System.out.println(HipchatUserHelper.getFullUser(allUsers.getItems().get(0), hc));

        User erik = HipchatUserAPI.getUser(hc, 31744);
        System.out.println(erik);

    }
}
