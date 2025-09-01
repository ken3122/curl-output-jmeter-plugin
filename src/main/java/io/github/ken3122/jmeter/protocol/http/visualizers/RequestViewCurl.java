package io.github.ken3122.jmeter.protocol.http.visualizers;

import java.awt.BorderLayout;

import javax.swing.*;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import com.google.auto.service.AutoService;
import org.apache.jmeter.gui.util.JSyntaxSearchToolBar;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RequestView;
import org.apache.jorphan.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RequestView implementation that shows the HTTP request as a cURL command.
 */

@AutoService(RequestView.class)
public class RequestViewCurl implements RequestView {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestViewCurl.class);

    static final String KEY_LABEL = "Curl";

    private JSyntaxTextArea curlStringField;
    private JPanel paneCurl;

    @Override
    public void clearData() {
        curlStringField.setInitialText("");
    }

    @Override
    public JPanel getPanel() {
        return paneCurl;
    }

    @Override
    public String getLabel() {
        return "Curl";
    }

    @Override
    public void init() {
        paneCurl  = new JPanel(new BorderLayout(0, 5));

        curlStringField = JSyntaxTextArea.getInstance(20, 80, true);
        curlStringField.setEditable(false);
        curlStringField.setLineWrap(true);
        curlStringField.setWrapStyleWord(true);
        JPanel requestAndSearchPanel = new JPanel(new BorderLayout());

        requestAndSearchPanel.add(new JSyntaxSearchToolBar(curlStringField).getToolBar(), BorderLayout.NORTH);
        requestAndSearchPanel.add(JTextScrollPane.getInstance(curlStringField), BorderLayout.CENTER);

        paneCurl.add(GuiUtils.makeScrollPane(requestAndSearchPanel));
    }

    @Override
    public void setSamplerResult(Object objectResult) {
        if (objectResult instanceof HTTPSampleResult) {
            HTTPSampleResult sampleResult = (HTTPSampleResult) objectResult;

            // Start building curl command
            StringBuilder curlCmd = new StringBuilder("curl -X ");

            // HTTP Method
            String method = sampleResult.getHTTPMethod();
            if (method != null && !method.isEmpty()) {
                curlCmd.append(method).append(" ");
            } else {
                curlCmd.append("GET "); // default
            }

            // URL
            String url = sampleResult.getUrlAsString();
            if (url != null && !url.isEmpty()) {
                curlCmd.append("\"").append(url).append("\" ");
            }

            // Headers
            String rh = sampleResult.getRequestHeaders();
            if (rh != null && !rh.isEmpty()) {
                String[] headers = rh.split("\n");
                for (String header : headers) {
                    if (!header.trim().isEmpty()) {
                        curlCmd.append("-H \"").append(header.trim()).append("\" ");
                    }
                }
            }

            // Body / Payload
            String data = sampleResult.getQueryString();
            if (data != null && !data.isEmpty()) {
                // For POST/PUT/PATCH requests, treat query string as body
                if (!"GET".equalsIgnoreCase(sampleResult.getHTTPMethod())) {
                    curlCmd.append("--data '")
                            .append(data.replace("'", "\\'"))
                            .append("' ");
                } else {
                    // For GET requests, append query string to URL instead
                    if (url != null && !url.isEmpty()) {
                        if (!url.contains("?")) {
                            curlCmd.append("\"").append(url).append("?").append(data).append("\" ");
                        } else {
                            curlCmd.append("\"").append(url).append("&").append(data).append("\" ");
                        }
                    }
                }
            }

            // Set the generated curl command in the UI field
            curlStringField.setText(curlCmd.toString().trim());
            curlStringField.setCaretPosition(0);
        } else {
            curlStringField.setText(JMeterUtils.getResString("view_results_table_request_http_nohttp"));
        }

    }

}