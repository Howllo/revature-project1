package net.revature.project1.controller;

import net.revature.project1.dto.UserSearchDto;
import net.revature.project1.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/search")
public class SearchController {
    final private SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService){
        this.searchService = searchService;
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<UserSearchDto>> searchForUser(@PathVariable String username){
        return ResponseEntity.ok(searchService.getSearchUser(username));
    }
}