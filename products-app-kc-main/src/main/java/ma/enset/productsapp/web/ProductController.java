package ma.enset.productsapp.web;

import lombok.Data;
import ma.enset.productsapp.repositories.ProductRepository;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ProductController {
    @Autowired
    private KeycloakRestTemplate keycloakRestTemplate;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }

    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        // HttpServletRequest request si vs utilisez le code commenté il faut ajouter ce parametre
        // using rest template on peut envoyer des http requests ms je doit ajouter le header authorization avec pour que ça marche

        // keycloak rest template va faire ce travail
        // il faut creer une methode au nv de l adapter : check KeycloakAdapterConfig
        /*KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext(); // là ou keycloak store les infos de sec du user authentifié
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + keycloakSecurityContext.getTokenString());
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<PagedModel<Supplier>> response = restTemplate.exchange
                ("http://localhost:8083/suppliers", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<PagedModel<Supplier>>() {
                });*/
        PagedModel<Supplier> pageSuppliers =keycloakRestTemplate.getForObject("http://localhost:8083/suppliers",PagedModel.class); // ajoute le header nichan // keycloak rest template b rasu herite de rest template de spring
        // je vaix communiquer les micro services ms avec un token dans la main pas comme openfeign
        model.addAttribute("suppliers",pageSuppliers);
        return "suppliers";
    }

    @GetMapping("/jwt")
    @ResponseBody
    // on a ajouter cette annotation pcq on a ici un controlleur et non pas un rest controller alors cette anno pour que le retour de la methode soit en json
    public Map<String, String> map(HttpServletRequest request) {
        // je cherche le jwt :
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext(); // là ou keycloak store les infos de sec du user authentifié
        Map<String, String> map = new HashMap<>();
        map.put("access_token", keycloakSecurityContext.getTokenString()); // voila le jwt -> tu peux recuperer ce que tu veux email ....
        return map;

    }

    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e,Model model){
        model.addAttribute("errorMessage","PROBLEME D'AUTHORISATIONS");
        return "errors";
    }

}
@Data
class Supplier{
    private Long id;
    private String name;
    private String email;
}