package service.auth.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {


    private String as;


    public EmailService(String as) {
        this.as = as;
    }
}
