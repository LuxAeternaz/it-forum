package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.dto.request.LoginRequest;
import luxaeterna.itforum.dto.request.RefreshRequest;
import luxaeterna.itforum.dto.request.RegisterRequest;
import luxaeterna.itforum.dto.response.LoginResponse;
import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.entity.User;
import luxaeterna.itforum.exception.BadRequestException;
import luxaeterna.itforum.exception.ResourceNotFoundException;
import luxaeterna.itforum.exception.UnauthorizedException;
import luxaeterna.itforum.mapper.CommentMapper;
import luxaeterna.itforum.mapper.FollowMapper;
import luxaeterna.itforum.mapper.LikeMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.mapper.UserMapper;
import luxaeterna.itforum.security.JwtTokenProvider;
import luxaeterna.itforum.service.UserService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final FollowMapper followMapper;
    private final LikeMapper likeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SnowflakeIdGenerator idGenerator;

    public UserServiceImpl(UserMapper userMapper, PostMapper postMapper, CommentMapper commentMapper,
                           FollowMapper followMapper, LikeMapper likeMapper,
                           PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                           SnowflakeIdGenerator idGenerator) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.followMapper = followMapper;
        this.likeMapper = likeMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userMapper.selectByEmail(request.getEmail()) != null) {
            throw new BadRequestException("该邮箱已被注册");
        }
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BadRequestException("该用户名已被使用");
        }

        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return new LoginResponse(token, refreshToken, user.getId(), user.getUsername(), user.getAvatarUrl(), user.getRole());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new UnauthorizedException("邮箱或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new UnauthorizedException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("邮箱或密码错误");
        }

        userMapper.updateLoginTime(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return new LoginResponse(token, refreshToken, user.getId(), user.getUsername(), user.getAvatarUrl(), user.getRole());
    }

    @Override
    public LoginResponse refreshToken(RefreshRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("refresh token无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new UnauthorizedException("用户不存在或已被禁用");
        }
        String newToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return new LoginResponse(newToken, newRefreshToken, user.getId(), user.getUsername(), user.getAvatarUrl(), user.getRole());
    }

    @Override
    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        user.setPostCount(postMapper.countByUserId(userId));
        user.setCommentCount(commentMapper.countByUserId(userId));
        user.setFollowerCount(followMapper.countFollowers(userId));
        user.setFollowingCount(followMapper.countFollowing(userId));
        user.setLikeReceivedCount(likeMapper.countLikesReceived(userId));
        return user;
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String username, String bio) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        if (username != null && !username.equals(user.getUsername())) {
            if (userMapper.selectByUsername(username) != null) {
                throw new BadRequestException("该用户名已被使用");
            }
            user.setUsername(username);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        userMapper.updateProfile(user);
        return user;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("原密码错误");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }

    @Override
    public String updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        return avatarUrl;
    }

    @Override
    public PageResult<Post> getUserPosts(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.selectByUserId(userId, offset, size);
        long total = postMapper.countByUserId(userId);
        return new PageResult<>(posts, total, page, size);
    }

    @Override
    public PageResult<Comment> getUserComments(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.selectByUserId(userId, offset, size);
        long total = commentMapper.countByUserId(userId);
        return new PageResult<>(comments, total, page, size);
    }
}
