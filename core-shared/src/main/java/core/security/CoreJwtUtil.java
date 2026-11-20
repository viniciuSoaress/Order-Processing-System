package core.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class CoreJwtUtil {

    private static final String ISSUER = "order-issuer-user";

    public static DecodedJWT validatedToken(String token, String secret){
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (Exception e){
            return null;
        }
    }
}
