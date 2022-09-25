package vttp2022.app.miniproject.Service;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp2022.app.miniproject.Model.PokemonAttribute;
import vttp2022.app.miniproject.Model.PokemonList;

@Service
public class PokemonService { 
    private static final Logger logger = LoggerFactory.getLogger(ToDoService.class);
    
    @Value("${pokemonTypes}")
    private String pokemonTypeURL;

    @Value("${pokemonName}")
    private String pokemonNameURL;

    @Value("${pokemonSpecies}")
    private String pokemonSpeciesURL;

    RestTemplate restTemplate = new RestTemplate();
    
    // ---------------------METHOD 1----------------------
    //
    //
    public List<String> getTypesList(){
        ResponseEntity<String> response = restTemplate.getForEntity(pokemonTypeURL, String.class);
        String payload = response.getBody();
        StringReader stringReader = new StringReader(payload);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        JsonArray jsonArray = jsonObject.getJsonArray("results");

        List<String> allTypes = new LinkedList<>();
        // 18 Types exclude two unknown
        for (int i = 0; i < 18; i++){
            JsonObject jsonOb = jsonArray.get(i).asJsonObject();

            String type = jsonOb.getString("name");
            String capPokeType = type.substring(0, 1).toUpperCase() 
                                + (type.substring(1));
            allTypes.add(capPokeType);  
        } 
        return allTypes;
    } 

    // ---------------------METHOD 2----------------------
    //
    //
    public List<PokemonAttribute> getPokemonDisplay(){
        //Get Pokemon Name limit to 20 for now
        ResponseEntity<String> response = restTemplate.getForEntity((pokemonNameURL + "/?offset=0&limit=8"), String.class);
        String payload = response.getBody();
        StringReader stringR = new StringReader(payload);
        JsonReader jsonR = Json.createReader(stringR);
        JsonObject jsonObj = jsonR.readObject();
        JsonArray jsonArr = jsonObj.getJsonArray("results");

        List<PokemonAttribute> pokeAttList = new LinkedList<>();

        for (int i = 0; i < jsonArr.size(); i++){
            JsonObject jsonOb = jsonArr.get(i).asJsonObject();

            PokemonAttribute pokeAt = new PokemonAttribute();
            String pokeName = jsonOb.getString("name");

            // Change Name to First Letter Caps
            String capPokeName = pokeName.substring(0, 1).toUpperCase() + pokeName.substring(1);
            pokeAt.setName(capPokeName);
            
            //Use Pokemon Name to get new URL 
            ResponseEntity<String> respN = restTemplate.getForEntity((pokemonNameURL + "/" + pokeName), String.class);
            String payloadN = respN.getBody();
            StringReader stringRN = new StringReader(payloadN);
            JsonReader jsonRN = Json.createReader(stringRN);
            JsonObject jsonObjN = jsonRN.readObject();

            //Getting Pokemon Img
            JsonObject sprites = jsonObjN.getJsonObject("sprites");
            JsonObject other = sprites.getJsonObject("other");
            JsonObject offArt = other.getJsonObject("official-artwork");
            
            pokeAt.setImageUrl(offArt.getString("front_default"));
            // logger.info(pokeAt.getImageUrl());

            //Getting Pokemon Types
            JsonArray jsonArrayN = jsonObjN.getJsonArray("types");
            List<String> pokeTypes = new LinkedList<>();
            for (int y = 0; y < jsonArrayN.size(); y++){
                JsonObject types = jsonArrayN.get(y).asJsonObject();
                
                JsonObject type = types.getJsonObject("type");
                String pokeType = type.getString("name");
                String capPokeType = pokeType.substring(0, 1).toUpperCase() + pokeType.substring(1);
                pokeTypes.add(capPokeType);                
            }
            pokeAt.setTypes(pokeTypes);
            pokeAttList.add(pokeAt);

        }
        PokemonList pokemonList = new PokemonList();
        pokemonList.setDisplayList(pokeAttList);

        return pokemonList.getDisplayList() ;  
    }

    // ---------------------METHOD 3----------------------
    //
    //
    public List<PokemonAttribute> getPokemonDetails(String name){
        
        List<PokemonAttribute> sPokeAttList = new LinkedList<>();
        // Input Name
        PokemonAttribute pokeDetails = new PokemonAttribute();

        String pokeSearchName = name.toLowerCase(); 
        
        ResponseEntity<String> respD = restTemplate.getForEntity((pokemonNameURL + "/" + pokeSearchName), String.class);
        String payloadD = respD.getBody();
        StringReader stringRD = new StringReader(payloadD);
        JsonReader jsonRD = Json.createReader(stringRD);
        JsonObject jsonObjD = jsonRD.readObject();
        JsonArray jsonArray = jsonObjD.getJsonArray("forms");
        JsonObject firstForm = jsonArray.get(0).asJsonObject();
        String nameById = firstForm.getString("name");
        
        String capPokeName = nameById.substring(0, 1).toUpperCase() + nameById.substring(1);
        pokeDetails.setName(capPokeName);

        String height = (jsonObjD.getJsonNumber("height")).toString();
        pokeDetails.setHeight(height);
        String weight = jsonObjD.getJsonNumber("weight").toString();
        pokeDetails.setWeight(weight);

        //Getting Pokemon Img
        JsonObject sprites = jsonObjD.getJsonObject("sprites");
        JsonObject other = sprites.getJsonObject("other");
        JsonObject offArt = other.getJsonObject("official-artwork");
        pokeDetails.setImageUrl(offArt.getString("front_default"));

        //Getting Pokemon Types
        JsonArray jsonArrayN = jsonObjD.getJsonArray("types");
        List<String> sPokeTypes = new LinkedList<>();
        for (int y = 0; y < jsonArrayN.size(); y++){
            JsonObject types = jsonArrayN.get(y).asJsonObject();
            
            JsonObject type = types.getJsonObject("type");
            String capPokeType = (type.getString("name")).substring(0, 1).toUpperCase() 
                                + (type.getString("name")).substring(1);
            sPokeTypes.add(capPokeType);                
        };
        pokeDetails.setTypes(sPokeTypes);
        
        //Getting Pokemon Abilities
        JsonArray jsonArrayA = jsonObjD.getJsonArray("abilities");
        List<String> sPokeAbi = new LinkedList<>();
        for (int y = 0; y < jsonArrayA.size(); y++){
            JsonObject abilities = jsonArrayA.get(y).asJsonObject();
            
            JsonObject ability = abilities.getJsonObject("ability");
            String capPokeAbi = (ability.getString("name")).substring(0, 1).toUpperCase() 
                                + (ability.getString("name")).substring(1);
            sPokeAbi.add(capPokeAbi);                
        };
        pokeDetails.setAbilities(sPokeAbi);
        for(String ability : pokeDetails.getAbilities()){
            logger.info(ability);
        }

        ResponseEntity<String> respDesc = restTemplate.getForEntity((pokemonSpeciesURL + "/" + pokeSearchName), String.class);
        String payloadDesc = respDesc.getBody();
        StringReader stringRDesc = new StringReader(payloadDesc);
        JsonReader jsonRDesc = Json.createReader(stringRDesc);
        JsonObject jsonObjDesc = jsonRDesc.readObject();
        JsonArray jsonArrDesc = jsonObjDesc.getJsonArray("flavor_text_entries");
        
        JsonObject lang = jsonArrDesc.get(0).asJsonObject();
        String desc = lang.getString("flavor_text").replace("\f", " ");
        logger.info(desc);
        pokeDetails.setDescription(desc);
        sPokeAttList.add(pokeDetails);

        
        PokemonList pokeDetailsList = new PokemonList();
        pokeDetailsList.setDetailsList(sPokeAttList);

        
        return pokeDetailsList.getDetailsList();
    }

    // ---------------------METHOD 4----------------------
    //
    //
    public List<PokemonAttribute> getPokemonDisplayFromType(String type){

        String pokeType = type.toLowerCase();

        
        ResponseEntity<String> responseT = restTemplate.getForEntity(pokemonTypeURL + "/" + pokeType, String.class);
        String payloadT = responseT.getBody();
        StringReader stringReaderT = new StringReader(payloadT);
        JsonReader jsonReaderT = Json.createReader(stringReaderT);
        JsonObject jsonObjectT = jsonReaderT.readObject();
        JsonArray jsonArrayT = jsonObjectT.getJsonArray("pokemon");
        List<PokemonAttribute> pokeAttList = new LinkedList<>();
        for (int i = 0; i < 20; i++){
            JsonObject jsonObj = jsonArrayT.get(i).asJsonObject();
            JsonObject pokemon = jsonObj.getJsonObject("pokemon");    
            String pokeN = pokemon.getString("name");
           
            PokemonAttribute pokeAt = new PokemonAttribute();
            // Change Name to First Letter Caps
            String capPokeName = pokeN.substring(0, 1).toUpperCase() + pokeN.substring(1);
            pokeAt.setName(capPokeName);

            //Use Pokemon Name to get new URL 
            ResponseEntity<String> respD = restTemplate.getForEntity((pokemonNameURL + "/" + pokeN), String.class);
            String payloadD = respD.getBody();
            StringReader stringRD = new StringReader(payloadD);
            JsonReader jsonRD = Json.createReader(stringRD);
            JsonObject jsonObjD = jsonRD.readObject();

            // //Getting Pokemon Img
            JsonObject sprites = jsonObjD.getJsonObject("sprites");
            JsonObject other = sprites.getJsonObject("other");
            JsonObject offArt = other.getJsonObject("official-artwork");
            pokeAt.setImageUrl(offArt.getString("front_default"));

            //Getting Pokemon Types
            JsonArray jsonArrayN = jsonObjD.getJsonArray("types");
            List<String> sPokeTypes = new LinkedList<>();
            for (int y = 0; y < jsonArrayN.size(); y++){
                JsonObject types = jsonArrayN.get(y).asJsonObject();
                
                JsonObject type1 = types.getJsonObject("type");
                String capPokeType = (type1.getString("name")).substring(0, 1).toUpperCase() 
                                    + (type1.getString("name")).substring(1);
                sPokeTypes.add(capPokeType);                
            };
            pokeAt.setTypes(sPokeTypes);
            pokeAttList.add(pokeAt);      
        }
    PokemonList pokeDisplayList = new PokemonList();
    pokeDisplayList.setDisplayList(pokeAttList);
    return pokeDisplayList.getDisplayList();
    }

    // ---------------------METHOD 5----------------------
    //
    //
    public PokemonAttribute getMyPokemon(String name){
        
        PokemonAttribute pokeDetails = new PokemonAttribute();

        String pokeSearchName = name.toLowerCase(); 
        String capPokeName = pokeSearchName.substring(0, 1).toUpperCase() + pokeSearchName.substring(1);
        pokeDetails.setName(capPokeName);
        ResponseEntity<String> respD = restTemplate.getForEntity((pokemonNameURL + "/" + pokeSearchName), String.class);
        String payloadD = respD.getBody();
        StringReader stringRD = new StringReader(payloadD);
        JsonReader jsonRD = Json.createReader(stringRD);
        JsonObject jsonObjD = jsonRD.readObject();

        //Getting Pokemon Img
        JsonObject sprites = jsonObjD.getJsonObject("sprites");
        JsonObject other = sprites.getJsonObject("other");
        JsonObject offArt = other.getJsonObject("official-artwork");
        pokeDetails.setImageUrl(offArt.getString("front_default"));

        //Getting Pokemon Types
        JsonArray jsonArrayN = jsonObjD.getJsonArray("types");
        List<String> sPokeTypes = new LinkedList<>();
        for (int y = 0; y < jsonArrayN.size(); y++){
            JsonObject types = jsonArrayN.get(y).asJsonObject();
            
            JsonObject type = types.getJsonObject("type");
            String capPokeType = (type.getString("name")).substring(0, 1).toUpperCase() 
                                + (type.getString("name")).substring(1);
            sPokeTypes.add(capPokeType);                
        };
        pokeDetails.setTypes(sPokeTypes);

        return pokeDetails;
    }
}