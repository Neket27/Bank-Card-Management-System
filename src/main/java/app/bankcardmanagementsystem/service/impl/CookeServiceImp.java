package app.bankcardmanagementsystem.service.impl;


import app.bankcardmanagementsystem.controller.dto.cooke.CreateCookeDto;
import app.bankcardmanagementsystem.service.CookeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookeServiceImp implements CookeService {

    private final HttpServletResponse httpServletResponse;

    @Override
    public void createCooke(CreateCookeDto createCookeDto){
        Cookie cookie = new Cookie(createCookeDto.getKey(), createCookeDto.getData());
        cookie.setPath(createCookeDto.getPath());
        cookie.setMaxAge(createCookeDto.getTimeLiveCooke());
        httpServletResponse.addCookie(cookie);
        httpServletResponse.setContentType(createCookeDto.getContentType());
    }
}
