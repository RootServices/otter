package org.rootservices.otter.security.csrf.between;


import org.rootservices.otter.controller.entity.Cookie;
import org.rootservices.otter.controller.entity.Request;
import org.rootservices.otter.controller.entity.Response;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.router.entity.Between;
import org.rootservices.otter.router.entity.Method;
import org.rootservices.otter.router.exception.CsrfException;
import org.rootservices.otter.router.exception.HaltException;
import org.rootservices.otter.security.csrf.DoubleSubmitCSRF;

import java.util.List;
import java.util.Optional;

public class CheckCSRF<S, U, P> implements Between<S, U, P> {
    private String cookieName;
    private String formFieldName;
    private DoubleSubmitCSRF doubleSubmitCSRF;
    private static String HALT_MSG = "CSRF failed.";

    public CheckCSRF(DoubleSubmitCSRF doubleSubmitCSRF) {
        this.doubleSubmitCSRF = doubleSubmitCSRF;
    }

    public CheckCSRF(String cookieName, String formFieldName, DoubleSubmitCSRF doubleSubmitCSRF) {
        this.cookieName = cookieName;
        this.formFieldName = formFieldName;
        this.doubleSubmitCSRF = doubleSubmitCSRF;
    }

    @Override
    public void process(Method method, Request<S, U, P> request, Response<S> response) throws HaltException {
        Boolean ok;
        Cookie csrfCookie = request.getCookies().get(cookieName);
        List<String> formValue = request.getFormData().get(formFieldName);
        if ( csrfCookie != null && formValue != null && formValue.size() == 1) {
            ok = doubleSubmitCSRF.doTokensMatch(csrfCookie.getValue(), formValue.get(0));
        } else {
            ok = false;
        }

        if(!ok) {
            CsrfException haltException = new CsrfException(HALT_MSG);
            onHalt(haltException, response);
            throw haltException;
        } else {
            request.setCsrfChallenge(Optional.of(formValue.get(0)));
        }
    }

    /**
     * This method will be called before a Halt Exception is thrown.
     * Override this method if you wish to change the behavior on the
     * response right before a Halt Exception is going to be thrown.
     * An Example would be, you may want to redirect the user to a login page.
     *
     * @param e a HaltException
     * @param response a Response
     */
    protected void onHalt(HaltException e, Response response) {
        response.setStatusCode(StatusCode.FORBIDDEN);
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getFormFieldName() {
        return formFieldName;
    }

    public void setFormFieldName(String formFieldName) {
        this.formFieldName = formFieldName;
    }

    public DoubleSubmitCSRF getDoubleSubmitCSRF() {
        return doubleSubmitCSRF;
    }

    public void setDoubleSubmitCSRF(DoubleSubmitCSRF doubleSubmitCSRF) {
        this.doubleSubmitCSRF = doubleSubmitCSRF;
    }
}
