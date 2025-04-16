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
        Cookie cookie = new Cookie(createCookeDto.getKey(), createCookeDto.getData());//создаем объект Cookie,в конструкторе указываем значения для name и value
        cookie.setPath(createCookeDto.getPath());//устанавливаем путь
        cookie.setMaxAge(createCookeDto.getTimeLiveCooke());//здесь устанавливается время жизни куки
        httpServletResponse.addCookie(cookie);//добавляем Cookie в запрос
        httpServletResponse.setContentType(createCookeDto.getContentType());//устанавливаем контекст
    }
}
