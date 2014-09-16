package com.oskarnyberg.chp.Main;

import java.io.File;
import java.util.Scanner;

public class TerminalUI implements PrinterListener {

	private static final String FLAG_ONESIDED = "-s";
	private static final String FLAG_DOUBLESIDED = "-d";
    private static final String FLAG_AMOUNT = "-#";
	private static final String FLAG_HELP = "help";
	private static final String FLAG_DASHHELP = "-help";
	private static final String FLAG_PRINTERS = "printers";
	private static final String FLAG_SETCID = "savecid";
	private static final String FLAG_SETPASSWORD = "savepassword";
	private static final String FLAG_GETCID = "getcid";
	private static final String FLAG_CLEARUSERDATA = "clearuserdata";
	private static final String FLAG_CLEARPRINTERS = "clearprinters";
	private static final String FLAG_EXTRA = "-x";

	private final Printer printer;

	public TerminalUI(String[] args) {
		printer = new Printer();
		printer.addListener(this);
		handleInput(args);
		printer.removeListener(this);
	}

	public void handleInput(String[] args) {
		boolean extra = false;
		for (String arg : args) {
			switch (arg) {
				case FLAG_SETCID:
					printer.setCid(queryCid(), true);
					return;
				case FLAG_SETPASSWORD:
					printer.setPassword(queryPassword(), true);
					return;
				case FLAG_GETCID:
					pl(printer.getCid());
					return;
				case FLAG_CLEARUSERDATA:
					printer.clearuserdata();
					return;
				case FLAG_CLEARPRINTERS:
					printer.clearprinters();
					return;
				case FLAG_HELP:
				case FLAG_DASHHELP:
					showHelp();
					return;
				case FLAG_PRINTERS:
					showPrinters();
					return;
				case FLAG_EXTRA:
					extra = true;
					break;
				case FLAG_ONESIDED:
					printer.setDoubleSided(false);
					break;
				case FLAG_DOUBLESIDED:
					printer.setDoubleSided(true);
					break;
				default:
                    if (arg.indexOf(FLAG_AMOUNT) == 0) {
                        try {
                            printer.setAmount(Integer.parseInt(arg.replace(FLAG_AMOUNT, "")));
                        } catch (NumberFormatException e) {
                            pl("Number of pages is not a number.");
                        }
                    } else if (extra) {
						printer.addExtra(arg);
						extra = false;
					} else if (new File(arg).isFile()) {
						printer.addFile(arg);
					} else {
						printer.setPrinter(arg.toUpperCase());
					}
					break;
			}
		}

		if (!printer.isFileSet()) {
			fileNotFound();
			return;
		} else if (!printer.isPrinterSet()) {
			noPrinterSpecified();
			return;
		}

		if (!printer.isCidSet()) {
			printer.setCid(queryCid(), false);
		}
		if (!printer.isPasswordSet()) {
			printer.setPassword(queryPassword(), false);
		}

		printer.print();
	}

	private void showHelp() {
		pl("For easy use, add an alias: chp='java -jar <PATH>/chp.jar'");
		pl("");
		pl("Commands:");
		pl("\t'" + FLAG_HELP + "': Shows help.");
		pl("\t'" + FLAG_PRINTERS + "': Lists previously used printers.");
		pl("\t'" + FLAG_SETCID + "': Save cid.");
		pl("\t'" + FLAG_SETPASSWORD + "': Save password.");
		pl("\t'" + FLAG_GETCID + "': Print cid.");
		pl("\t'" + FLAG_CLEARUSERDATA + "': Remove saved cid and password.");
		pl("\t'" + FLAG_CLEARPRINTERS + "': Remove saved printers.");
		pl("\t'" + FLAG_ONESIDED + "': Single sided.");
		pl("\t'" + FLAG_DOUBLESIDED + "': Double sided.");
        pl("\t'" + FLAG_AMOUNT + "n': Number of copies, replace n with the number of copies you want.");
		pl("\t'" + FLAG_EXTRA + "': Follow with extra flags to send with the lpr command. Write commands as a string enclosed with quotes.");
		pl("");
		pl("Example:");
		pl("\tchp -d file.pdf ED-3349A-LASER1");
		pl("");

		showPrinters();
	}

	private void showPrinters() {
		pl("Previously used printers:");
		int i = 0;
		for (String printer : this.printer.getPrinters()) {
			pl("\t" + (++i) + ". " + printer);
		}
		pl("You can use the index of a printer instead of name.");
	}

	private String queryCid() {
		pl("CID:");
		return new Scanner(System.in).nextLine();
	}

	private String queryPassword() {
		return new String(System.console().readPassword("%s", "Password:"));
	}

	private void pl(String string) {
		System.out.println(string);
	}

    private void p(String string) {
        System.out.print(string);
    }

	@Override
	public void uploadFailed() {
		pl("Fileupload failed.");
	}

	@Override
	public void connecting() {
		p("Connecting...");
	}

	@Override
	public void noPrinterSpecified() {
		pl("No printer specified.");
	}

	@Override
	public void fileNotFound() {
		pl("File not found.");
	}

	@Override
	public void noCidSet() {
		pl("Cid not set.");
	}

	@Override
	public void noPasswordSet() {
		pl("Password not set.");
	}

	@Override
	public void invalidPrinterIndex() {
		pl("Invalid printer index.");
	}

	@Override
	public void printSucceeded() {
		pl("Done! :D");
	}

	@Override
	public void uploading() {
		p("Uploading...");
	}

	@Override
	public void sendingPrintCommand() {
		p("Sending print command...");
	}

    @Override
    public void started(String file) {
        pl("File: " + file);
    }

    @Override
	public void connectionFailed() {
		pl("Cannot connect to server.");
	}

	@Override
	public void authorizationFailed() {
		pl("Login failed.");
	}

	@Override
	public void executionFailed(String error) {
		p("Print failed (" + error + ")");
	}
}
