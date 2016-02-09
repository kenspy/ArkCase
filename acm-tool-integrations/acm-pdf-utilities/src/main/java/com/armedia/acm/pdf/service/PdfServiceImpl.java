package com.armedia.acm.pdf.service;

import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.pdf.PdfServiceException;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.mule.api.MuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * PDF document generator built upon XSL-FO library (Apache FOP).
 * Created by Petar Ilin <petar.ilin@armedia.com> on 02.10.2015.
 */
public class PdfServiceImpl implements PdfService
{
    /**
     * Logger instance.
     */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Ecm file service.
     */
    private EcmFileService ecmFileService;

    /**
     * Random number generator.
     */
    private Random random = new Random();

    /**
     * Generate PDF file based on XSL-FO stylesheet, XML data source and replacement parameters.
     * NOTE: the caller is responsible for deleting the file afterwards (not to leave mess behind)
     *
     * @param xslFile    XSL-FO stylesheet
     * @param source     XML data source required for XML transformation
     * @param parameters a key-value map of parameters to be replaced in the stylesheet
     * @return path to the newly generated PDF file (random filename stored in temp folder)
     * @throws PdfServiceException on PDF creation error
     */
    @Override
    public String generatePdf(File xslFile, Source source, Map<String, String> parameters) throws PdfServiceException
    {
        // create a temporary file name
        String filename = String.format("%s/acm-%020d.pdf", System.getProperty("java.io.tmpdir"), Math.abs(random.nextLong()));
        log.debug("PDF creation: using [{}] as temporary file name", filename);
        try
        {
            // Base URI is where the FOP engine will be resolving URIs against
            URI baseURI = xslFile.getParentFile().toURI();
            log.debug("Using [{}] as FOP base URI", baseURI);
            FopFactory fopFactory = FopFactory.newInstance(baseURI);
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslFile));

            for (Map.Entry<String, String> parameter : parameters.entrySet())
            {
                String name = parameter.getKey();
                // transformation fails on null values
                String value = (parameter.getValue() != null) ? parameter.getValue() : "N/A";
                transformer.setParameter(name, value);
            }

            OutputStream os = null;
            try
            {
                os = new BufferedOutputStream(new FileOutputStream(filename));
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
                Result result = new SAXResult(fop.getDefaultHandler());
                transformer.transform(source, result);
            } finally
            {
                if (os != null)
                {
                    os.close();
                }
            }
        } catch (FOPException | TransformerException | IOException e)
        {
            log.error("Unable to generate PDF document", e);
            throw new PdfServiceException(e);
        }
        return filename;
    }

    /**
     * Generate PDF file based on XSL-FO stylesheet, XML data source and replacement parameters.
     * NOTE: the caller is responsible for deleting the file afterwards (not to leave mess behind)
     *
     * @param xslFilename path to XSL-FO stylesheet
     * @param source      XML data source required for XML transformation
     * @param parameters  a key-value map of parameters to be replaced in the stylesheet
     * @return path to the newly generated PDF file (random filename stored in temp folder)
     * @throws PdfServiceException on PDF creation error
     */
    @Override
    public String generatePdf(String xslFilename, Source source, Map<String, String> parameters) throws PdfServiceException
    {
        File xslFile = new File(xslFilename);
        return generatePdf(xslFile, source, parameters);
    }

    /**
     * Generate PDF file based on XSL-FO stylesheet and replacement parameters (no XML data source).
     * NOTE: the caller is responsible for deleting the file afterwards (not to leave mess behind)
     *
     * @param xslFile    XSL-FO stylesheet
     * @param parameters a key-value map of parameters to be replaced in the stylesheet
     * @return path to the newly generated PDF file (random filename stored in temp folder)
     * @throws PdfServiceException on PDF creation error
     */
    @Override
    public String generatePdf(File xslFile, Map<String, String> parameters) throws PdfServiceException
    {
        Source source = new DOMSource();
        return generatePdf(xslFile, source, parameters);
    }

    /**
     * Generate PDF file based on XSL-FO stylesheet and replacement parameters (no XML data source).
     * NOTE: the caller is responsible for deleting the file afterwards (not to leave mess behind)
     *
     * @param xslFilename path to XSL-FO stylesheet
     * @param parameters  a key-value map of parameters to be replaced in the stylesheet
     * @return path to the newly generated PDF file (random filename stored in temp folder)
     * @throws PdfServiceException on PDF creation error
     */
    @Override
    public String generatePdf(String xslFilename, Map<String, String> parameters) throws PdfServiceException
    {
        Source source = new DOMSource();
        return generatePdf(xslFilename, source, parameters);
    }

    /**
     * Append one PDF file to another (incremental merge).
     *
     * @param pdDocument       source PDF, the document we are appending to
     * @param is               input stream of the document we are appending
     * @param pdfMergerUtility PDF merger utility
     * @return merged document
     * @throws PdfServiceException on error while merging
     */
    @Override
    public PDDocument append(PDDocument pdDocument, InputStream is, PDFMergerUtility pdfMergerUtility) throws PdfServiceException
    {
        try
        {
            if (pdDocument == null) // first document
            {
                pdDocument = PDDocument.load(is);
            } else
            {
                PDDocument next = PDDocument.load(is);
                pdfMergerUtility.appendDocument(pdDocument, next);
                next.close();
            }
        } catch (IOException e)
        {
            log.error("Unable to generate PDF document", e);
            throw new PdfServiceException(e);
        }
        return pdDocument;
    }

    /**
     * Append one PDF file to another (incremental merge).
     *
     * @param pdDocument       source PDF, the document we are appending to
     * @param ecmFile          document we are appending as ECM file
     * @param pdfMergerUtility PDF merger utility
     * @return merged document
     * @throws PdfServiceException on error while merging
     */
    @Override
    public PDDocument append(PDDocument pdDocument, EcmFile ecmFile, PDFMergerUtility pdfMergerUtility) throws PdfServiceException
    {
        InputStream is = null;
        try
        {
            is = ecmFileService.downloadAsInputStream(ecmFile.getFileId());
        } catch (MuleException | AcmUserActionFailedException e)
        {
            throw new PdfServiceException(e);
        }
        return append(pdDocument, is, pdfMergerUtility);
    }

    /**
     * Append one PDF file to another (incremental merge).
     *
     * @param pdDocument       source PDF, the document we are appending to
     * @param filename         path to the document we are appending
     * @param pdfMergerUtility PDF merger utility
     * @return merged document
     * @throws PdfServiceException on error while merging
     */
    @Override
    public PDDocument append(PDDocument pdDocument, String filename, PDFMergerUtility pdfMergerUtility) throws PdfServiceException
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(filename);
        } catch (FileNotFoundException e)
        {
            throw new PdfServiceException(e);
        }
        return append(pdDocument, is, pdfMergerUtility);
    }

    /**
     * Generates multipage TIFF from PDF file.
     *
     * @param inputPdf   pdf file to be processed
     * @param outputTiff location where generated TIFF to be saved
     * @throws PdfServiceException on error generating TIFF
     */
    @Override
    public void generateTiffFromPdf(File inputPdf, File outputTiff) throws PdfServiceException
    {
        if (!inputPdf.exists())
        {
            throw new PdfServiceException(inputPdf.getAbsolutePath() + " doesn't exists");
        }

        ImageOutputStream ios = null;
        PDDocument document = null;
        try
        {
            ios = ImageIO.createImageOutputStream(outputTiff);
            document = PDDocument.load(inputPdf);

            log.debug("Reading pdf from file path {}", outputTiff.getPath());

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            TIFFImageWriterSpi tiffspi = new TIFFImageWriterSpi();
            ImageWriter writer = tiffspi.createWriterInstance();
            log.debug("Preparing to generate multi image tiff.");
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            log.debug("Pdf contains {} pages.", document.getNumberOfPages());
            int page = 0;


            TIFFImageWriteParam writeParam = new TIFFImageWriteParam(Locale.getDefault());
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType("JPEG");

            //full quality! could be: from 0.1f to 1.0f
            writeParam.setCompressionQuality(0.75f);

            for (PDPage pdPage : document.getPages())
            {
                // using 150dpi as the lowest acceptable resolution
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                IIOImage image = new IIOImage(bim, null, null);
                try
                {
                    writer.writeToSequence(image, writeParam);
                } catch (UnsupportedOperationException e)
                {
                    log.warn("Not supported compression:", e);
                    writer.writeToSequence(image, null);
                }
                log.debug("Successfully written one image to the sequence, {} more to go.", document.getNumberOfPages() - page);
                page++;
            }
            ios.flush();
        } catch (IOException e)
        {
            throw new PdfServiceException(e);
        } finally
        {
            IOUtils.closeQuietly(document);
            IOUtils.closeQuietly(ios);
        }
        log.debug("Successfully written tiff sequence into file {}. With length: {} MBytes", outputTiff.getPath(), ((float) outputTiff.length() / (1024 * 1024)));
    }

    /**
     * Add source for merging into single PDF document.
     *
     * @param pdfMergerUtility PDF merger utility
     * @param is               input stream of the document we are appending
     * @throws PdfServiceException on error adding source stream
     */
    public void addSource(PDFMergerUtility pdfMergerUtility, InputStream is) throws PdfServiceException
    {
        pdfMergerUtility.addSource(is);
    }

    /**
     * Add source for merging into single PDF document.
     *
     * @param pdfMergerUtility PDF merger utility
     * @param ecmFile          document we are appending as ECM file
     * @throws PdfServiceException on error adding source ECM file
     */
    public void addSource(PDFMergerUtility pdfMergerUtility, EcmFile ecmFile) throws PdfServiceException
    {
        InputStream is = null;
        try
        {
            is = ecmFileService.downloadAsInputStream(ecmFile.getFileId());
        } catch (MuleException | AcmUserActionFailedException e)
        {
            throw new PdfServiceException(e);
        }
        addSource(pdfMergerUtility, is);
    }

    /**
     * Add source for merging into single PDF document.
     *
     * @param pdfMergerUtility PDF merger utility
     * @param filename         path to the document we are appending
     * @throws PdfServiceException on error adding source file
     */
    public void addSource(PDFMergerUtility pdfMergerUtility, String filename) throws PdfServiceException
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(filename);
        } catch (FileNotFoundException e)
        {
            throw new PdfServiceException(e);
        }
        addSource(pdfMergerUtility, is);
    }

    /**
     * Merge multiple sources into single PDF document.
     *
     * @param pdfMergerUtility PDF merger utility
     * @param filename         path to the merged document
     * @throws PdfServiceException on error creating merged document
     */
    public void mergeSources(PDFMergerUtility pdfMergerUtility, String filename) throws PdfServiceException
    {
        // using at most 32MB memory, the rest goes to disk
        MemoryUsageSetting memoryUsageSetting = MemoryUsageSetting.setupMixed(1024 * 1024 * 32);
        try
        {
            pdfMergerUtility.setDestinationFileName(filename);
            pdfMergerUtility.mergeDocuments(memoryUsageSetting);
        } catch (IOException e)
        {
            throw new PdfServiceException(e);
        }
    }

    public EcmFileService getEcmFileService()
    {
        return ecmFileService;
    }

    public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }
}
