package org.exoplatform.portal.mop.management.operations;

import org.gatein.management.api.exceptions.NotAuthorizedException;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class SecureOperationHandler implements OperationHandler {
    @Override
    public final void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException {
        // Secure all operations for MOP Management extension to /platform/administrators group.
        if (!operationContext.getExternalContext().isUserInRole("administrators")) {
            throw new NotAuthorizedException(operationContext.getUser(), operationContext.getOperationName());
        }

        doExecute(operationContext, resultHandler);
    }

    protected abstract void doExecute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException;
}
