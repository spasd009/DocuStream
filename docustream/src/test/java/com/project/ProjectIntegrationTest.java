package com.project;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
@SpringBootTest(properties={"spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:tests;MODE=MySQL;DB_CLOSE_DELAY=-1}","spring.datasource.driver-class-name=${TEST_DATABASE_DRIVER:org.h2.Driver}","spring.datasource.username=${TEST_DATABASE_USER:sa}","spring.datasource.password=${TEST_DATABASE_PASSWORD:}","spring.sql.init.schema-locations=${TEST_SCHEMA:classpath:test-schema.sql}"})
@AutoConfigureMockMvc
class ProjectIntegrationTest {
 @Autowired ProjectService service; @Autowired JdbcTemplate db; @Autowired MockMvc mvc;
 @BeforeEach void reset(){db.update("DELETE FROM watchlist");db.update("DELETE FROM users");service.register("driver","secure-pass-123");service.register("alice","secure-pass-123");service.register("bob","secure-pass-123");}
 @Test void publicPagesAndAuthenticatedPagesRender() throws Exception {
  for(String path:new String[]{"/","/docs","/docs/1","/login","/register"})mvc.perform(get(path)).andExpect(status().isOk());
  for(String path:new String[]{"/account","/watchlist"})mvc.perform(get(path).with(user("alice"))).andExpect(status().isOk());
  mvc.perform(get("/account")).andExpect(status().is3xxRedirection());
 }
 @Test void registrationLoginAndCsrf() throws Exception {
  mvc.perform(post("/register").param("username","charlie").param("password","long-password-123")).andExpect(status().isForbidden());
  mvc.perform(post("/register").with(csrf()).param("username","charlie").param("password","long-password-123")).andExpect(redirectedUrl("/login"));
  mvc.perform(post("/login").with(csrf()).param("username","charlie").param("password","long-password-123")).andExpect(redirectedUrl("/account"));
  String hash=db.queryForObject("SELECT password FROM users WHERE username='charlie'",String.class);assertNotEquals("long-password-123",hash);
 }
 @Test void searchWatchlistAndValidation(){assertEquals(1,service.docs("Planet","").size());assertEquals(1,service.docs("","Science").size());service.watch("alice",1,false);service.watch("alice",1,false);assertEquals(1,service.watchlist("alice").size());service.watch("alice",1,true);assertTrue(service.watchlist("alice").isEmpty());assertThrows(IllegalArgumentException.class,()->service.register("x","short"));}
 @Test void errorViewsRender() throws Exception {service.watch("alice",1,false);mvc.perform(get("/watchlist").with(user("alice"))).andExpect(status().isOk());mvc.perform(get("/docs/9999")).andExpect(status().isBadRequest());}
 @Test void privateDataAndSecurityHeaders() throws Exception {
  String body=mvc.perform(get("/account").with(user("alice")))
   .andExpect(status().isOk())
   .andExpect(header().string("X-Frame-Options","DENY"))
   .andExpect(header().string("X-Content-Type-Options","nosniff"))
   .andExpect(header().string("Referrer-Policy","no-referrer"))
   .andExpect(header().string("Permissions-Policy","camera=(), microphone=(), geolocation=()"))
   .andExpect(header().string("Content-Security-Policy",org.hamcrest.Matchers.containsString("frame-ancestors 'none'")))
   .andExpect(header().string("Cache-Control",org.hamcrest.Matchers.containsString("no-store")))
   .andReturn().getResponse().getContentAsString();
  String hash=db.queryForObject("SELECT password FROM users WHERE username='alice'",String.class);
  assertFalse(body.contains(hash));assertFalse(body.contains("secure-pass-123"));assertFalse(body.contains("jdbc:"));
  for(String path:new String[]{"/application.properties","/schema.sql","/pom.xml","/.env","/h2-console"})mvc.perform(get(path).with(user("alice"))).andExpect(status().isNotFound());
 }
 @Test void passwordByteLimitIsValidated(){assertThrows(UserInputException.class,()->service.register("unicode_user","é".repeat(40)));}

 @Test void watchlistsArePrivateAndExternalMediaCannotLoad() {
  service.watch("alice",1,false);assertTrue(service.watchlist("bob").isEmpty());
  String original=db.queryForObject("SELECT media FROM documentaries WHERE id=1",String.class);
  try{db.update("UPDATE documentaries SET media=? WHERE id=1","https://example.invalid/private-key.mp4");assertEquals("/media/unavailable.mp4",service.doc(1).get("MEDIA"));}finally{db.update("UPDATE documentaries SET media=? WHERE id=1",original);}
 }

}
