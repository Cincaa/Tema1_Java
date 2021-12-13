package ro.unibuc.springlab8example1.repository;

import com.zaxxer.hikari.pool.HikariProxyCallableStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import ro.unibuc.springlab8example1.domain.User;
import ro.unibuc.springlab8example1.domain.UserDetails;
import ro.unibuc.springlab8example1.domain.UserType;
import ro.unibuc.springlab8example1.exception.UserNotFoundException;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class UserRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public User save(User user) {
        String saveUserSql = "INSERT INTO users (username, full_name, user_type, account_created) VALUES (?,?,?,?)";
        jdbcTemplate.update(saveUserSql, user.getUsername(), user.getFullName(), user.getUserType().name(), LocalDateTime.now());

        User savedUser = getUserWith(user.getUsername());
        UserDetails userDetails = user.getUserDetails();

        if (userDetails != null) {
            String saveUserDetailsSql = "INSERT INTO user_details (cnp, age, other_information) VALUES (?, ?, ?)";
            jdbcTemplate.update(saveUserDetailsSql, userDetails.getCnp(), userDetails.getAge(), userDetails.getOtherInformation());

            UserDetails savedUserDetails = getUserDetailsWith(userDetails.getCnp());
            savedUser.setUserDetails(savedUserDetails);

            String saveUsersUserDetails = "INSERT INTO users_user_details (users, user_details) VALUES (?, ?)";
            jdbcTemplate.update(saveUsersUserDetails, savedUser.getId(), savedUserDetails.getId());
        }

        return savedUser;
    }

    public User get(String username) {
        String selectDetailsSql = "SELECT u.id, u.username, u.full_name, ud.cnp, ud.age, ud.other_information, u.user_type, u.account_created FROM users u JOIN user_details ud ON u.id = ud.id where u.username = ?";
        RowMapper<User> rowMapper = (resultSet, rowNo) -> User.builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .fullName(resultSet.getString("full_name"))
                .userDetails(new UserDetails(resultSet.getLong("id"),resultSet.getString("cnp"), resultSet.getInt("age"), resultSet.getString("other_information")))
                .userType(UserType.valueOf(resultSet.getString("user_type")))
                .accountCreated(Instant.ofEpochMilli(resultSet.getDate("account_created").getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();

        List<User> users = jdbcTemplate.query(selectDetailsSql, rowMapper, username);
        if (!users.isEmpty()) {
            return users.get(0);
        }

        return getUserWith(username);
    }

    private User getUserWith(String username) {
        String selectSql = "SELECT * from users WHERE users.username = ?";
        RowMapper<User> rowMapper = (resultSet, rowNo) -> User.builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .fullName(resultSet.getString("full_name"))
                .userType(UserType.valueOf(resultSet.getString("user_type")))
                .build();

        List<User> users = jdbcTemplate.query(selectSql, rowMapper, username);

        if (!users.isEmpty()) {
            return users.get(0);
        }

        throw new UserNotFoundException("User not found");
    }

    private UserDetails getUserDetailsWith(String cnp) {
        String selectSql = "SELECT * from user_details WHERE user_details.cnp = ?";
        RowMapper<UserDetails> rowMapper = (resultSet, rowNo) -> UserDetails.builder()
                .id(resultSet.getLong("id"))
                .cnp(resultSet.getString("cnp"))
                .age(resultSet.getInt("age"))
                .otherInformation(resultSet.getString("other_information"))
                .build();

        List<UserDetails> details = jdbcTemplate.query(selectSql, rowMapper, cnp);

        if (!details.isEmpty()) {
            return details.get(0);
        }

        throw new UserNotFoundException("User details not found");
    }

    public User update(User user){
        MapSqlParameterSource parameterUser = new MapSqlParameterSource()
                .addValue("id",user.getId())
                .addValue("username",user.getUsername())
                .addValue("fullName",user.getFullName());
        MapSqlParameterSource parameterUserDetails = new MapSqlParameterSource()
                .addValue("id",user.getUserDetails().getId())
                .addValue("cnp",user.getUserDetails().getCnp())
                .addValue("age",user.getUserDetails().getAge())
                .addValue("otherInformation",user.getUserDetails().getOtherInformation());

        String updateUserSQL = "update users set user_name=:username, full_name=:fullName where id=:id";
        String updateUserDetailsSQL = "update user_details set cnp=:cnp, age=:age, other_information=:otherInformation where id=:id";

        int countUser = jdbcTemplate.update(updateUserSQL,parameterUser);
        int countUserDetail = jdbcTemplate.update(updateUserDetailsSQL,parameterUserDetails);
        if(countUser > 0 || countUserDetail > 0){
            return user;
        }
        throw new UserNotFoundException("User can't update");
    }

    public boolean delete(long id){
        String deleteSQL = "delete from users_user_details where id=?";
        String deleteUserSQL = "delete from users where id=?";
        String deleteUserDetailsSQL = "delete from user_details where id=?";
        int count = jdbcTemplate.update(deleteSQL,id);
        count = jdbcTemplate.update(deleteUserDetailsSQL,id);
        count = jdbcTemplate.update(deleteUserSQL,id);
        return count > 0 ? true : false;
    }

    public List<User> getByType(String type){
        String findSQL = "SELECT * FROM users where user_type =?";
        RowMapper<User> rowMapper = (resultSet, rowNo) -> User.builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .fullName(resultSet.getString("full_name"))
                .userType(UserType.valueOf(resultSet.getString("user_type")))
                .build();
        List<User> users = jdbcTemplate.query(findSQL, rowMapper, type);
        if (!users.isEmpty()) {
            return users;
        }
        throw new UserNotFoundException("Users with this type don't exists");
    }
}
