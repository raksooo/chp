package com.oskarnyberg.chp.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

public class Printer {

	private enum SidesEnum {
		ONE_SIDED, DOUBLE_SIDED
	}

	private static final String CID_STRING = "cid";
	private static final String PASSWORD_STRING = "password";
	private static final String PRINTERS_STRING = "printers";

	private static final String CHALMERS_SERVER_ADDRESS = "remote2.student.chalmers.se";

	private final PrinterListener listener;
	private final List<PrinterListener> listeners;

	private final Preferences prefs;
	private String username;
	private String password;

	private ArrayList<File> files;
	private String printer;
	private SidesEnum sides;
    private int amount;
	private String extra;

	public Printer() {
		prefs = Preferences.userRoot().node("com.oskarnyberg.chp.Main.Printer");

		listener = new PrinterListenerImpl();
		listeners = new ArrayList<>();

        files = new ArrayList<>();
        extra = "";

		username = prefs.get(CID_STRING, null);
		password = prefs.get(PASSWORD_STRING, null);
		if (password != null) {
			password = Xor.decryptString(password);
		}
	}

	public void clearprinters() {
		prefs.remove(PRINTERS_STRING);
	}

	public void clearuserdata() {
		prefs.remove(CID_STRING);
		prefs.remove(PASSWORD_STRING);
	}

	public void print() {
		if (username == null) {
			listener.noCidSet();
			return;
		} else if (password == null) {
			listener.noPasswordSet();
			return;
		} else if (printer == null) {
			listener.noPrinterSpecified();
			return;
		}
        for (File file : files) {
            if (!file.isFile()) {
                listener.fileNotFound();
                return;
            }
        }

		savePrinter(printer);
        for (File file : files) {
            listener.started(file.getName());
		    ssh(file);
        }
	}

	private void ssh(File file) {
		String tmpname = "tmp" + System.currentTimeMillis() + file.getName().substring(file.getName().lastIndexOf('.'));

		listener.connecting();
		SSH ssh = new SSH(CHALMERS_SERVER_ADDRESS, username, password);
		ssh.addListener(listener);
		try {
			listener.uploading();
			ssh.getSSHClient().newSCPFileTransfer().upload(file.getPath(), tmpname);
			listener.sendingPrintCommand();
			ssh.execute(createPrintString(tmpname));
			ssh.execute("rm " + tmpname);
			ssh.disconnect();
			listener.printSucceeded();
			ssh.removeListener(listener);
		} catch (IOException e) {
			listener.uploadFailed();
		} catch (IllegalStateException e) {
			listener.connectionFailed();
			System.exit(1);
		}
	}

	private String createPrintString(String file) {
		StringBuilder string = new StringBuilder("lpr");
		if (sides == SidesEnum.DOUBLE_SIDED) {
			string.append(" -o sides=two-sided-long-edge");
		} else if (sides == SidesEnum.ONE_SIDED) {
			string.append(" -o sides=one-sided");
		}
		string.append(" -P ");
		string.append(printer);
		if (extra != null) {
			string.append(" ");
			string.append(extra);
		}
		string.append(" ");
		string.append(file);

		return string.toString();
	}

	public void addFile(String file) {
		if (new File(file).exists()) {
			this.files.add(new File(file));
		}
	}

	public void setPrinter(String printer) {
		try {
			this.printer = getPrinters().get(Integer.parseInt(printer)-1);
		} catch (NumberFormatException e) {
			this.printer = printer;
		} catch (IndexOutOfBoundsException e) {
			listener.invalidPrinterIndex();
		}
	}

	public void setDoubleSided(boolean doubleSided) {
		sides = doubleSided ? SidesEnum.DOUBLE_SIDED : SidesEnum.ONE_SIDED;
	}

    public void setAmount(int amount) {
        this.amount = amount;
    }

	public void addExtra(String option) {
		extra += " " + option;
	}

	public void setCid(String cid, boolean save) {
		username = cid;
		if (save) {
			prefs.put(CID_STRING, cid);
		}
	}

	public void setPassword(String password, boolean save) {
		this.password = password;
		if (save) {
			prefs.put(PASSWORD_STRING, Xor.encryptString(this.password));
		}
	}

	public String getCid() {
		return prefs.get(CID_STRING, "Cid not set!");
	}

	public boolean isCidSet() {
		return username != null;
	}

	public boolean isPasswordSet() {
		return password != null;
	}

	public boolean isFileSet() {
		return files.size() > 0;
	}

	public boolean isPrinterSet() {
		return printer != null;
	}

	private void savePrinter(String printer) {
		List<String> printers = getPrinters();
		if (!printers.contains(printer)) {
			printers.add(printer);
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (String p : printers) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(",");
			}
			stringBuilder.append(p);
		}
		prefs.put(PRINTERS_STRING, stringBuilder.toString());
	}

	public List<String> getPrinters() {
		String printersString = prefs.get(PRINTERS_STRING, "");
		if (printersString.length() > 0) {
			return new ArrayList<>(Arrays.asList(printersString.split(",")));
		} else {
			return new ArrayList<>();
		}
	}

	public void addListener(PrinterListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PrinterListener listener) {
		listeners.remove(listener);
	}

	private class PrinterListenerImpl implements PrinterListener {

		@Override
		public void uploadFailed() {
			for (PrinterListener listener : listeners) {
				listener.uploadFailed();
			}
		}

		@Override
		public void connecting() {
			for (PrinterListener listener : listeners) {
				listener.connecting();
			}
		}

		@Override
		public void noPrinterSpecified() {
			for (PrinterListener listener : listeners) {
				listener.noPrinterSpecified();
			}
		}

		@Override
		public void fileNotFound() {
			for (PrinterListener listener : listeners) {
				listener.fileNotFound();
			}
		}

		@Override
		public void noCidSet() {
			for (PrinterListener listener : listeners) {
				listener.noCidSet();
			}
		}

		@Override
		public void noPasswordSet() {
			for (PrinterListener listener : listeners) {
				listener.noPasswordSet();
			}
		}

		@Override
		public void invalidPrinterIndex() {
			for (PrinterListener listener : listeners) {
				listener.invalidPrinterIndex();
			}
		}

		@Override
		public void printSucceeded() {
			for (PrinterListener listener : listeners) {
				listener.printSucceeded();
			}
		}

		@Override
		public void sendingPrintCommand() {
			for (PrinterListener listener : listeners) {
				listener.sendingPrintCommand();
			}
		}

        @Override
        public void started(String file) {
            for (PrinterListener listener : listeners) {
                listener.started(file);
            }
        }

        @Override
		public void uploading() {
			for (PrinterListener listener : listeners) {
				listener.uploading();
			}
		}

		@Override
		public void connectionFailed() {
			for (PrinterListener listener : listeners) {
				listener.connectionFailed();
			}
		}

		@Override
		public void authorizationFailed() {
			for (PrinterListener listener : listeners) {
				listener.authorizationFailed();
			}
		}

		@Override
		public void executionFailed(String error) {
			for (PrinterListener listener : listeners) {
				listener.executionFailed(error);
			}
		}
	}
}