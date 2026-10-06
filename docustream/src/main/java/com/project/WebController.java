package com.project;
import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class WebController {
 private final ProjectService service;
 public WebController(ProjectService service){this.service=service;}
 @ModelAttribute("signedIn") boolean signedIn(Principal p){return p!=null;}
 @GetMapping("/") String home(){return "home";}
 @GetMapping("/docs") String docs(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String genre,Model m){m.addAttribute("items",service.docs(q,genre));m.addAttribute("q",q);m.addAttribute("genre",genre);return "docs";}
 @GetMapping("/docs/{id}") String documentary(@PathVariable long id,Model m){m.addAttribute("doc",service.doc(id));return "documentary";}
 @GetMapping("/login") String login(){return "login";}
 @GetMapping("/register") String register(){return "register";}
 @PostMapping("/register") String register(@RequestParam String username,@RequestParam String password,RedirectAttributes flash){service.register(username,password);flash.addFlashAttribute("message","Account created. Please sign in.");return "redirect:/login";}
 @PostMapping("/watchlist/{id}") String watch(@PathVariable long id,@RequestParam(defaultValue="false") boolean remove,Principal p){service.watch(p.getName(),id,remove);return "redirect:/watchlist";}
 @GetMapping("/watchlist") String watchlist(Principal p,Model m){m.addAttribute("items",service.watchlist(p.getName()));return "watchlist";}
 @GetMapping("/account") String account(Principal p,Model m){m.addAttribute("items",service.watchlist(p.getName()));return "account";}
 @ExceptionHandler({IllegalArgumentException.class,java.time.DateTimeException.class,org.springframework.dao.DataIntegrityViolationException.class}) String invalid(Exception e,Model m,Principal p,jakarta.servlet.http.HttpServletResponse response){m.addAttribute("signedIn",p!=null);response.setStatus(400);m.addAttribute("problem",e instanceof UserInputException?e.getMessage():"Please check the information you entered and try again.");return "problem";}
}
