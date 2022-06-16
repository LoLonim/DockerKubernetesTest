package com.nexign.testapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Controller
public class MainController {
    @RequestMapping("/{filePath}")
    @ResponseBody
    public String fileView(@PathVariable("filePath") String filepath){
        String result;
        try{
            result = Files.readString(Path.of("./"+filepath));
        }
        catch (IOException e){
            result = "File not found or unable to read";
        }
        return result;
    }
    @RequestMapping("/")
    public String fileTree(Model model){
        File file = new File("./");
        model.addAttribute("files", Arrays.stream(file.list()).toList());
        return "mainPage";
    }
}
