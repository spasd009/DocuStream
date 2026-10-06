package com.project;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProjectService {
 private final JdbcTemplate db; private final PasswordEncoder passwords;
 public ProjectService(JdbcTemplate db,PasswordEncoder passwords){this.db=db;this.passwords=passwords;}
 @Transactional public void register(String name,String password){
  if(!name.matches("[A-Za-z0-9_]{3,40}")||password.length()<10||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new UserInputException("Use a username of 3–40 letters, digits or underscores and a password of 10–72 characters.");
  if(!db.queryForList("SELECT username FROM users WHERE username=?",name).isEmpty())throw new UserInputException("Username already exists.");
  db.update("INSERT INTO users VALUES (?,?)",name,passwords.encode(password));
 }
 public List<Map<String,Object>> docs(String q,String genre){return db.queryForList("SELECT id,title,genre,description,media FROM documentaries WHERE LOWER(title) LIKE ? AND LOWER(genre) LIKE ? ORDER BY id", "%"+q.toLowerCase(Locale.ROOT)+"%","%"+genre.toLowerCase(Locale.ROOT)+"%");}
 public Map<String,Object> doc(long id){var result=db.queryForList("SELECT id,title,genre,description,media FROM documentaries WHERE id=?",id).stream().findFirst().orElseThrow(()->new UserInputException("Documentary not found."));String media=String.valueOf(result.get("MEDIA"));if(!media.matches("/media/[A-Za-z0-9_-]+\\.mp4"))result.put("MEDIA","/media/unavailable.mp4");return result;}
 @Transactional public void watch(String name,long id,boolean remove){doc(id);if(remove)db.update("DELETE FROM watchlist WHERE username=? AND documentary_id=?",name,id);else db.update("INSERT INTO watchlist(username,documentary_id) VALUES (?,?) ON DUPLICATE KEY UPDATE documentary_id=VALUES(documentary_id)",name,id);}
 public List<Map<String,Object>> watchlist(String name){return db.queryForList("SELECT d.id,d.title,d.genre,d.description,d.media FROM documentaries d JOIN watchlist w ON d.id=w.documentary_id WHERE w.username=? ORDER BY d.id",name);}

}
