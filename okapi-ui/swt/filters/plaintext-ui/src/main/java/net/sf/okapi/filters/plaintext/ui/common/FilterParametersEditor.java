package net.sf.okapi.filters.plaintext.ui.common;

import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.abstracteditor.AbstractParametersEditor;
import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.lib.extra.filters.CompoundFilterParameters;

public abstract class FilterParametersEditor extends AbstractParametersEditor {

	@Override
	protected boolean loadParameters() {

		// Iterate through parameters of sub-filters and pages to load default
		// values into the pages

		if (getParams() instanceof CompoundFilterParameters) {

			List<IParameters> list = ((CompoundFilterParameters) getParams()).getParameters();

			for (IParameters parameters : list)
				for (IDialogPage page : getPages())
					page.load(parameters);
		}

		// Iterate through pages, load parameters

		for (IDialogPage page : getPages()) {

			if (page == null)
				return false;

			if (!page.load(getParams())) {

				Dialogs.showError(getShell(), String.format("Error loading parameters to the %s page.",
						getCaption(page)), null);
				return false; // The page unable to load params is invalid
			}
		}

		if (getParams() instanceof CompoundFilterParameters) {

			IParameters activeParams = ((CompoundFilterParameters) getParams()).getActiveParameters();

			for (IDialogPage page : getPages()) {

				if (page == null)
					return false;

				if (!page.load(activeParams)) {

					Dialogs.showError(getShell(), String
							.format("Error loading parameters to the %s page.", getCaption(page)), null);
					return false; // The page unable to load params is invalid
				}
			}
		}

		for (IDialogPage page : getPages()) {

			if (page == null)
				return false;

			page.interop(null);
		}

		interop(null);

		return true;
	}

	@Override
	protected boolean saveParameters() {
		// Iterate through pages, store parameters

		if (isReadOnly()) {

			Dialogs.showWarning(getShell(), "Editor in read-only mode, parameters are not saved.", null);
			return false;
		}

		for (IDialogPage page : getPages()) {

			if (page == null)
				return false;

			page.interop(null);
		}

		interop(null);

		for (IDialogPage page : getPages()) {

			if (page == null)
				return false;
			if (!page.save(getParams())) { // Fills in parametersClass

				Dialogs.showError(getShell(), String.format("Error saving parameters from the %s page.",
						getCaption(page)),
						null);
				return false;
			}
		}

		if (getParams() instanceof CompoundFilterParameters) {

			IParameters activeParams = ((CompoundFilterParameters) getParams()).getActiveParameters();

			for (IDialogPage page : getPages()) {

				if (page == null)
					return false;

				if (!page.save(activeParams)) {

					Dialogs.showError(getShell(), String.format("Error saving parameters from the %s page.",
							getCaption(page)), null);
					return false;
				}
			}
		}

		return true;
	}

}
