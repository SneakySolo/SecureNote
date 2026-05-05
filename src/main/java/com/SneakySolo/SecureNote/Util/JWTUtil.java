package com.SneakySolo.SecureNote.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long duration;

    public String generateToken(String username){
        Map<String, Object> claims = new HashMap<>(); // we can add role, email and other details inside

        return Jwts.builder() // allows us to start building token
                .claims() // adding custom data to the token
                .add(claims) // adding custom data to the token
                .subject(username) // adding main identity which we later extract to match with user from db
                .issuedAt(new Date(System.currentTimeMillis())) // when token was created (added current time)
                .expiration(new Date(System.currentTimeMillis() + duration)) // setting expiration time
                .and()
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // signing this token with secret key using HS256
                .compact(); // THIS CONVERTS EVERYTHING TO A JWT STRING
    }

    // so our secret key is in a form of string, but security needs it in a form of 'Key'
    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // so we decode the string key to bytes
        return Keys.hmacShaKeyFor(keyBytes); // then convert those bytes to an object of type 'Key'
    }

    // we put the subject of the token as username, so we extract it for validation purposes
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // instead of writing many methods, we wrote one for all
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // get all the claims in the form of 'Claims' object
        return claimsResolver.apply(claims); // apply function on them

        // note - 'Claims' is a map like object that has everything in the token in JSON format
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser() // a jwt has 3 components to it, so this - Split the string, Decode each part and Validate everything
                .verifyWith((SecretKey) getSigningKey()) // verifies the signature using our secret key
                .build() // finalizes the parser configuration
                .parseSignedClaims(token) // decodes token and encoded string turns into JSON so signature can be verified
                .getPayload(); // if success then we get a Jws<Claims> object which has everything

        // this will throw exception if signature doesn't match
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String userName = extractUserName(token);
        Date expDate = extractClaim(token, Claims::getExpiration);

        if (userName.equals(userDetails.getUsername())
            && expDate.after(new Date())) {
                return true;
        }
        return false;
    }
}
