
package hawaiiappbuilders.c;

public class URLResolver {

   // private static final String BASE_URL = "http://Chuck.com/CJL";

    // User Relations
   // private static final String Login = "/login.php";
   // private static final String Register = "/RegisterCJL.php";
   // private static final String UpdateToken = "/UpdateMyToken.php";
   // private static final String Logout = "/logout.php";

    // For Senders
    // private static final String DelFavsGet = "/DelFavsGet.php";
    // private static final String DelAdd = "/DelAdd.php";
    // private static final String DelFavsAdd = "/DelFavsAdd.php";
    // private static final String DelBidsGet = "/DelBidsGet.php";
    // private static final String Beep = "/Beep.php";
    // private static final String DelGetDriverTokensByDriverID = "/DelGetDriverTokensByDriverID.php";
    // private static final String DelTokensNameBySenderID = "/DelTokensNameBySenderID.php";

    // private static final String DelStatusRateDriv = "/DelStatusRateDriv.php";

    // For Drivers
    // private static final String DelGetID = "/DelGetID.php";
    // private static final String DelsInArea = "/DelsInArea.php";
    // private static final String DelsByDriverID = "/DelsByDriverID.php";
    // private static final String DelsBySenderID = "/DelsBySenderID.php";
    // private static final String DelBidAdd = "/DelBidAdd.php";
    // private static final String DelAccept = "/DelAccept.php";
   // private static final String DelStatusUpdate = "/DelStatusUpdate.php";
    // private static final String DelStatusRate = "/DelStatusRate.php";

    // private static final String DelGetTokensBySenderID = "/DelGetTokensBySenderID.php";
    // private static final String DelSenderTokensByDelID = "/DelSenderTokensByDelID.php";

   // private static final String DelsGet = "/DelsGet.php";
   // private static final String DelDriverAdd = "/DelDriverAdd.php";
   // private static final String DelDriversLocUpdate = "/DelDriversLocUpdate.php";
   // private static final String DelDriversLocGet = "/DelDriversLocGet.php";


  //   public static final String DELTRANSACTION = "/listTXsCJL.php";

    // -------------------- User Relations ---------------------------------------------------------
    /*public static final String apiLogin(JSONObject dataObj) {
        String data = "data={\"devid\":\"1434741\",\"user_name\":\"guest\",\"password\":\"12345\",\"uuid\":\"681B07CB-CC5B-48DC68595\",\"token\":\"fxNCpZ7q4Ak:Af5DhyS\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"rrr\":\"0\"}";

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s%s?data=%s", BASE_URL, Login, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiRegister(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s%s?data=%s", BASE_URL, Register, encoded.replaceAll("\\+", "%20"));
    }*/

   /* public static final String apiUpdateToken(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s%s?data=%s", BASE_URL, UpdateToken, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiLogout(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s%s?data=%s", BASE_URL, Logout, encoded.replaceAll("\\+", "%20"));
    }*/
    // ---------------------------------------------------------------------------------------------

    // -------------------- Sender Relations -------------------------------------------------------
 /*   public static final String apiDelFavsGet(JSONObject dataObj) {
        String data = "data={\"devid\":\"1434741\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"uuid\":\"681B07CB-CC5B-4305-8ADC-9B558DC68595\",\"userid\":\"11\"}";

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String fullUrl = String.format("%s%s?data=%s", BASE_URL, DelFavsGet, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, DelFavsGet, encoded.replaceAll("\\+", "%20"));
    }*/
/*
    public static final String apiDelAdd(JSONObject dataObj) {
        String data = "data={\"devid\":\"1434741\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"uuid\":\"681B07CB-CC5B-4305-8ADC-9B558DC68595\",\"userid\":\"11\"}";

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //String fullUrl = String.format("%s%s?data=%s", BASE_URL, DelAdd, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, DelAdd, encoded.replaceAll("\\+", "%20"));
    }*/

   /* public static final String apiDelFavsAdd() {
        String data = "data={\"devid\":\"1434741\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"uuid\":\"681B07CB-CC5B-4305-8ADC-9B558DC68595\",\"userid\":\"11\",\"driverid\":\"55\"}";
        *//*
        {
            "devid":"1434741",
            "appid":"MyFavDelAndroid",
            "lon":"1",
            "lat":"22.22222",
            "uuid":"681B07CB-CC5B-4305-8ADC-9B558DC68595",
            "userid":"11",
            "driverid":"55"
        }
        *//*

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("devid", "1434741");
            jsonRequest.put("appid", "MyFavDelAndroid");
            jsonRequest.put("lon", "1");
            jsonRequest.put("lat", "22.22222");
            jsonRequest.put("uuid", "681B07CB-CC5B-4305-8ADC-9B558DC68595");
            jsonRequest.put("userid", "11");
            jsonRequest.put("driverid", "55");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String formatUrl = String.format("%s%s?data=%s", BASE_URL, DelFavsAdd, jsonRequest.toString());
        return formatUrl;
    }*/
   /* public static final String apiDelBidsList(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelBidsGet, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiBeep(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String fullUrl = String.format("%s%s?data=%s", BASE_URL, Beep, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, Beep, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiDelGetDriverTokensByDriverID(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String fullUrl = String.format("%s%s?data=%s", BASE_URL, DelGetDriverTokensByDriverID, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, DelGetDriverTokensByDriverID, encoded.replaceAll("\\+", "%20"));
    }*/

   /* public static final String apiDelTokensNameBySenderID(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String fullUrl = String.format("%s%s?data=%s", BASE_URL, DelTokensNameBySenderID, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, DelTokensNameBySenderID, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiDelStatusUpdate(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String fullUrl = String.format("%s%s?data=%s", BASE_URL, DelStatusUpdate, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, DelStatusUpdate, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiDelStatusRateDriv(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String fullUrl = String.format("%s%s?data=%s", BASE_URL, DelStatusRateDriv, encoded.replaceAll("\\+", "%20"));
        return String.format("%s%s?data=%s", BASE_URL, DelStatusRateDriv, encoded.replaceAll("\\+", "%20"));
    }
*/
    /*public static final String apiDelsBySenderID(JSONObject dataObj) {
        String data = "data={\"devid\":\"1434741\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"uuid\":\"681B07CB-CC5B-4305-8ADC-9B558DC68595\",\"userid\":\"11\"}";

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelsBySenderID, encoded.replaceAll("\\+", "%20"));
    }*/
    // ---------------------------------------------------------------------------------------------

    // -------------------- Driver Relations -------------------------------------------------------
    /*public static final String apiDelGetID(JSONObject dataObj) {
        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelGetID, encoded.replaceAll("\\+", "%20"));
    }*/

  /*  public static final String apiDelsInArea(JSONObject dataObj) {
        String data = "data={\"devid\":\"1434741\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"uuid\":\"681B07CB-CC5B-4305-8ADC-9B558DC68595\",\"userid\":\"11\"}";

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelsInArea, encoded.replaceAll("\\+", "%20"));
    }*/

  /*  public static final String apiDelsByDriverID(JSONObject dataObj) {
        String data = "data={\"devid\":\"1434741\",\"appid\":\"MyFavDelAndroid\",\"lon\":\"1\",\"lat\":\"22.22222\",\"uuid\":\"681B07CB-CC5B-4305-8ADC-9B558DC68595\",\"userid\":\"11\"}";

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelsByDriverID, encoded.replaceAll("\\+", "%20"));
    }

    public static final String apiDelBidAdd(JSONObject dataObj) {

        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelBidAdd, encoded.replaceAll("\\+", "%20"));
    }*/

 /*   public static final String apiDelAccept(JSONObject dataObj) {
        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelAccept, encoded.replaceAll("\\+", "%20"));
    }*/

    /*public static final String apiDelStatusRate(JSONObject dataObj) {
        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelStatusRate, encoded.replaceAll("\\+", "%20"));
    }

    public static final String apiDelGetTokensBySenderID(JSONObject dataObj) {
        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelGetTokensBySenderID, encoded.replaceAll("\\+", "%20"));
    }

    public static final String apiDelSenderTokensByDelID(JSONObject dataObj) {
        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DelSenderTokensByDelID, encoded.replaceAll("\\+", "%20"));
    }
    // ---------------------------------------------------------------------------------------------

    public static final String apiTransactions(JSONObject dataObj) {
        String encoded = dataObj.toString().trim();
        try {
            encoded = URLEncoder.encode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return String.format("%s%s?data=%s", BASE_URL, DELTRANSACTION, encoded.replaceAll("\\+", "%20"));
    }*/

}

