package kh.com.csx.posapi.dto.user;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserIdConverter implements Converter<String, UserId> {
    @Override
    public UserId convert(String source){
        try{
            Long id = Long.parseLong(source);
            return new UserId(id);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid user format: " + source, e);
        }

    }
}
