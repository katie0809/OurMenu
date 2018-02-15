package com.uos.kyungimlee.ourmenu;

import java.util.ArrayList;

/**
 * Created by kyungimlee on 2017. 12. 6..
 */

class AllergenIngreList {

    private static final String milkyFood[] = {
            // 우유, 유제품
            "milk",
            "butter",
            "buttermilk",
            "casein",
            "cheese",
            // "cream",
            "curds",
            "custard",
            "diacetyl",
            "ghee",
            "half-and-half",    // 밀크티
            "lactalbumin",
            "phosphate",
            "lactoferrin",
            "lactose",
            "lactulose",
            "pudding",
            "sour cream",
            "tagatose",
            "whey",
            "yogurt",
            "margarine",
            "chocolate"
    };

    private static final String eggyFood[] = {
            "albumin",
            "albumen",
            "egg",
            "eggnog",
            "globulin",
            "livetin",
            "lysozyme",
            "mayonnaise",
            "meringue",
            "surimi",
            "vitellin",
            "ice cream",
            "marshmallows"
    };

    private static final String wheatyFood[] = {
            "bread",
            "bulgur",
            "cereal",
            "wheat",
            "couscous",
            "cracker",
            "durum",
            "einkorn",
            "emmer",
            "farina",
            "flour",
            "protein",
            "cake,",
            "graham",
            "gluten",
            "pastry",
            "Kamut",
            "matzoh",
            "pasta",
            "seitan",
            "semolina",
            "spelt",
            "triticale",
            "bran",
            "malt",
            "starch"
    };

    private static final String soyFood[] = {
            "edamame",
            "miso",
            "natto",
            "soy",
            "soya",
            "soybean",
            "shoyu",
            "tamari",
            "tempeh",
            "tofu"
    };

    private static final String shellFishyFood[] = {
            "barnacle",
            "crab",
            "crawfish",
            "crayfish",
            "ecrevisse",
            "krill",
            "lobster",
            "prawns",
            "shrimp",
            "scampi",
            "langoustine",
            "tomalley",
            "crevette",
            "abalone",
            "clams",
            "cherrystone",
            "geoduck",
            "littleneck",
            "pismo",
            "quahog",
            "cockle",
            "cuttlefish",
            "limpet",
            "mussels",
            "octopus",
            "periwinkle",
            "oysters",
            "scallops",
            "sea cucumber",
            "sea urchin",
            "snail",
            "escargot",
            "squid",
            "calamari",
            "whelk",
            "Turban shell"
    };

    private static final String peanutyFood[] = {
            "nut",
            "peanut",
            "goobers",
            "nougat",
            "Goober"
    };

    private static final String treeNutyFood[] = {
            "almond",
            "beechnut",
            "butternut",
            "cashew",
            "chestnut",
            "gianduja",
            "pecan",
            "pesto",
            "pistachio",
            "praline",
            "walnut"
    };

    private static final String fishyFood[] = {
            "barbecue sauce",
            "bouillabaisse",
            "Caesar salad",
            "caviar",
            "fish",
            "nuoc mam",
            "anchovy",
            "shark",
            "surimi",
            "sushi",
            "sashimi",
            "Bass",
            "Catfish",
            "Cod",
            "Flounder",
            "Grouper",
            "Haddock",
            "Hake",
            "Halibut",
            "Herring",
            "Mahi mahi",
            "Perch",
            "Pike",
            "Pollock",
            "Salmon",
            "Scrod",
            "Sole",
            "Snapper",
            "Swordfish",
            "Tilapia",
            "Trout",
            "Tuna"
    };

    private static final String sesamyFood[] = {
            "Benne",
            "benniseed",
            "Gingelly",
            "Gomasio",
            "Halvah",
            "Sesame",
            "Sesamol",
            "Sesamum",
            "Sesemolina",
            "Tahini"
    };

    private static final String otherAllergen[] = {
            "corn",
            "beef",
            "chicken",
            "mutton",
            "Gelatin",
            "sunflower seed",
            "poppy seed",
            "coriander",
            "garlic",
            "mustard",
            "apple",
            "carrot",
            "peach",
            "plum",
            "tomato",
            "banana"
    };

    // milky, eggy, wheaty, soy, shellfishy, peanuty, treeNuty, fishy, sesamy, other
    public String isThereAllergenIngre(ArrayList<String> ingredients) {
        StringBuilder temp = new StringBuilder();

        for(int i = 0; i < ingredients.size(); i++) {
            if(i == ingredients.size() - 1)
                temp.append(ingredients);
            else
                temp.append(ingredients + ", ");
        }

        return isThereAllergenIngre(temp.toString());
    }

    // milky, eggy, wheaty, soy, shellfishy, peanuty, treeNuty, fishy, sesamy, other
    public String isThereAllergenIngre(String ingredients) {
        StringBuilder retBuilder = new StringBuilder();
        String ingreToLower = ingredients.toLowerCase();

        for(int i = 0; i < milkyFood.length; i++) {
            if(ingreToLower.contains(milkyFood[i].toLowerCase()))
                retBuilder.append(milkyFood[i] + ", ");
        }

        for(int i = 0; i < eggyFood.length; i++) {
            if(ingreToLower.contains(eggyFood[i].toLowerCase()))
                retBuilder.append(eggyFood[i] + ", ");
        }

        for(int i = 0; i < wheatyFood.length; i++) {
            if(ingreToLower.contains(wheatyFood[i].toLowerCase()))
                retBuilder.append(wheatyFood[i] + ", ");
        }

        for(int i = 0; i < soyFood.length; i++) {
            if(ingreToLower.contains(soyFood[i].toLowerCase()))
                retBuilder.append(soyFood[i] + ", ");
        }

        for(int i = 0; i < shellFishyFood.length; i++) {
            if(ingreToLower.contains(shellFishyFood[i].toLowerCase()))
                retBuilder.append(shellFishyFood[i] + ", ");
        }

        for(int i = 0; i < peanutyFood.length; i++) {
            if(ingreToLower.contains(peanutyFood[i].toLowerCase()))
                retBuilder.append(peanutyFood[i] + ", ");
        }

        for(int i = 0; i < treeNutyFood.length; i++) {
            if(ingreToLower.contains(treeNutyFood[i].toLowerCase()))
                retBuilder.append(treeNutyFood[i] + ", ");
        }

        for(int i = 0; i < fishyFood.length; i++) {
            if(ingreToLower.contains(fishyFood[i].toLowerCase()))
                retBuilder.append(fishyFood[i] + ", ");
        }

        for(int i = 0; i < sesamyFood.length; i++) {
            if(ingreToLower.contains(sesamyFood[i].toLowerCase()))
                retBuilder.append(sesamyFood[i] + ", ");
        }

        for(int i = 0; i < otherAllergen.length; i++) {
            if(ingreToLower.contains(otherAllergen[i].toLowerCase()))
                retBuilder.append(otherAllergen[i] + ", ");
        }

        String ret = new String();
        if(retBuilder.length() > 0)
            ret = retBuilder.substring(0, retBuilder.length() - 2); // ", " 제거
        else
            ret = "There is not allergen";

        return ret;
    }
}
