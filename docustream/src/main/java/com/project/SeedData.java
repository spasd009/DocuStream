package com.project;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class SeedData implements CommandLineRunner {
 private final JdbcTemplate db;
 public SeedData(JdbcTemplate db){this.db=db;}
 public void run(String...args){
  if(db.queryForObject("SELECT COUNT(*) FROM documentaries",Integer.class)==0){
   db.update("INSERT INTO documentaries(title,genre,description,media) VALUES (?,?,?,?)","Our Changing Planet","Nature","Explore the natural world and the changes shaping its future.","/media/planet.mp4");
   db.update("INSERT INTO documentaries(title,genre,description,media) VALUES (?,?,?,?)","A Journey Through History","History","Explore how stories connect generations.","/media/history.mp4");
   db.update("INSERT INTO documentaries(title,genre,description,media) VALUES (?,?,?,?)","The Science of Tomorrow","Science","Discover the ideas shaping tomorrow.","/media/science.mp4");
  }
 }
}
