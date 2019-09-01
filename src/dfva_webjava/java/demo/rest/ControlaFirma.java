package dfva_webjava.java.demo.rest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dfva_java.client.Client;
import dfva_java.client.SettingsManager;
import dfva_webjava.java.demo.DfvaRespuesta;
import dfva_webjava.java.demo.DocumentSign;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class ControlaFirma {
	
	
	private DocumentSign get_document_sign(String name) {
		final ObjectMapper mapper = new ObjectMapper();
 
		DocumentSign doc= null;
		try {
			doc = mapper.readValue( new File("/tmp/demo_"+name),
					DocumentSign.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	 
		return doc;
	}
	
	private void _save_document(String name, DocumentSign  doc) throws IOException {
		String result = new ObjectMapper()
			      .writeValueAsString(doc);
		System.out.println(result);
		 File serverFile = new File("/tmp/demo_"+name);
		 System.out.println("Gyardando en "+"/tmp/demo_"+name);
		 BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
		 stream.write(result.getBytes());
		 stream.close();
	}
	
	private void save_document(String name, DocumentSign  doc) {
		try {
			_save_document(name, doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private SettingsManager manager = SettingsManager.getInstance();
    @RequestMapping(value = "/sign/{id}", method=RequestMethod.POST,
            produces="application/json" )
    @ResponseBody
    public ResponseEntity<DfvaRespuesta> firme(
    		  HttpSession session,
    		  @PathVariable("id") String id 
    		) {	
    		
    		DocumentSign doc = get_document_sign(id);
    		Map<String, String> values = doc.getValues();
    		Client client = new Client(manager.get_and_create_settings());
    		
     		String razon = null;
     		String lugar = null;
     		String doc_format = values.get("doc_format");
     		if (doc_format.equals("pdf")) {
     			razon = values.get("razon");
     			lugar = values.get("lugar");
     		}
 
    		
    		JsonObject signres = client.sign(
    				values.get("identificacion"), 
    				new ByteArrayInputStream(Base64.decodeBase64(values.get("file_uploaded"))), 
    			    doc_format,  // xml_cofirma, xml_contrafirma, odf, msoffice 
    			    values.get("resumen"),
    			    "sha512",
    			    lugar,
    			    razon
    			    );
 

	        int status = signres.getInt("status");
	        boolean success = status == 0;
	        
 
    		DfvaRespuesta persondata = new DfvaRespuesta(
    			success,  // fueExitosaLaSolicitud
    			240,   // tiempoMaximoDeFirmaEnSegundos
    			3,   // tiempoDeEsperaParaConsultarLaFirmaEnSegundos
    			signres.getString("code"),  // codigoDeVerificacion
    			signres.getInt("id_transaction"),  // idDeLaSolicitud
    			!success,  // debeMostrarElError
    			signres.getString("status_text"), //descripcionDelError
    			signres.getString("resume")  // ResumenDelDocumento
    		);
	                
    		return new ResponseEntity<DfvaRespuesta>(persondata,  HttpStatus.OK);
    }
    
    @RequestMapping(value = "/check_sign/{id}", method=RequestMethod.GET)
    public String check_firma(
    		HttpServletRequest request,
    		@RequestParam("callback") String callback,
    		@RequestParam("IdDeLaSolicitud") String IdDeLaSolicitud,
    		@RequestParam("_") String _nd,
    		HttpSession session,
    		@PathVariable("id") String id 
    		) {
    	JsonObject response=null;
    	Client client = new Client(manager.get_and_create_settings());
    	JsonObject signres = client.sign_check(IdDeLaSolicitud);
    	 
        int status = signres.getInt("status");
        
        boolean success = status == 0;
        boolean received_notification = signres.getBoolean("received_notification");
        if(success && received_notification ) {
        	DocumentSign doc = get_document_sign(id);
        	doc.add("doc_signed", signres.getString("sign_document"));
        	doc.add("hash_signed", signres.getString("hash_docsigned"));
        	
        	save_document(id, doc);
        }
       
        
        //System.out.println(signres.toString());
    	response = Json.createObjectBuilder()
    			.add("ExtensionData", "{}")
    			.add("DebeMostrarElError", !success)
    			.add("DescripcionDelError", signres.getString("status_text"))
    			.add("FueExitosa", success)
    			.add("SeRealizo", signres.getBoolean("received_notification")).build();     
    	return callback+"("+response.toString()+")";	 
    }
}
