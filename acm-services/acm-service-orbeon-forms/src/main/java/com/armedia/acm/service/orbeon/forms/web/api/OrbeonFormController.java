package com.armedia.acm.service.orbeon.forms.web.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.armedia.acm.service.orbeon.forms.model.ROIFormOrbeon;

@Controller
@RequestMapping("/api/v1/forms/crud/acm")
public class OrbeonFormController implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;
    private Logger log = LoggerFactory.getLogger(getClass());
	private Unmarshaller unmarshaller;
	private Marshaller marshaller;
	
    public Marshaller getMarshaller() {
		return marshaller;
	}
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	/**
     * The Orbeon request URL would look like this:
     * /crud/[APPLICATION_NAME]/[FORM_NAME]/(data|draft)/[FORM_DATA_ID]/data.xml
     * 
     * savemode: data | draft
     * 
     */
    @RequestMapping(value = "/{formname}/{savemode}/{formdataid}/{file}", method = RequestMethod.PUT)
    @ResponseBody
    public Object saveFormData(Authentication auth,
    		@PathVariable("formname") String formName,
    		@PathVariable("savemode") String saveMode,
    		@PathVariable("formdataid") String formDataId,
    		@PathVariable("file") String filename,
            HttpServletRequest request
            ) throws IOException, ParserConfigurationException, SAXException
    {

        log.info("Form name: " + formName + "; Save mode: " +
            saveMode + "; form data id: " + formDataId + "; File name: " + filename);

        if ( filename.equals("data") && request.getContentType().equals("application/xml") )
        {
            BufferedReader br = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while ( line != null )
            {
                sb.append(line).append("\r\n");
                line = br.readLine();
            }

            String formXml = sb.toString();
            log.debug("Request: " + formXml);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));
            log.debug("Doc XML Version: " + doc.getXmlVersion());

            convertFromXMLToObject(formXml);
            return formXml;
        }
        else
        {
            log.debug("got a content file, size = " + request.getContentLength() + ", type: " + request.getContentType());
            System.out.println("got a content file, size = " + request.getContentLength() + ", type: " + request.getContentType());
            return filename;
        }


    }
    
    private Object convertFromXMLToObject( String xmlString) throws IOException {
    	ByteArrayInputStream in = new ByteArrayInputStream(xmlString.getBytes());
        
        ROIFormOrbeon obj = null;
        try {
            obj = (ROIFormOrbeon) this.unmarshaller.unmarshal(new StreamSource(in));
        } finally {
            if (in != null) {
            	in.close();
            }
        }
		
		return obj;
	}
    

    public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	public ApplicationEventPublisher getApplicationEventPublisher()
    {
        return applicationEventPublisher;
    }

    @Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;		
	}
}
