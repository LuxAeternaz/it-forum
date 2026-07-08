package luxaeterna.itforum.service;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.dto.request.LoginRequest;
import luxaeterna.itforum.dto.request.RefreshRequest;
import luxaeterna.itforum.dto.request.RegisterRequest;
import luxaeterna.itforum.dto.response.LoginResponse;
import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.entity.User;

public interface UserService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshRequest request);
    User getProfile(Long userId);
    User updateProfile(Long userId, String username, String bio);
    void changePassword(Long userId, String oldPassword, String newPassword);
    String updateAvatar(Long userId, String avatarUrl);
    PageResult<Post> getUserPosts(Long userId, int page, int size);
    PageResult<Comment> getUserComments(Long userId, int page, int size);
}
