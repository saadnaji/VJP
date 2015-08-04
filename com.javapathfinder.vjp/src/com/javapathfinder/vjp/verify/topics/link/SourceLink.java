package com.javapathfinder.vjp.verify.topics.link;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.javapathfinder.vjp.VJP;

public class SourceLink implements IHyperlink {
  
  private IRegion region;
  private SourceAddress address;
  
  public SourceLink(IRegion region, SourceAddress address){
    this.region = region;
    this.address = address;
  }

  public IRegion getHyperlinkRegion() {
    return region;
  }

  public String getHyperlinkText() {
    return null;
  }

  public String getTypeLabel() {
    return null;
  }

  public void open() {
    if (address == null){
      VJP.logError("Could not open source file, address is null.");
      return;
    }
    
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    try {
      IFile file = address.file;
      int lineNumber = address.lineNum;
      IEditorPart part = IDE.openEditor(page, file);
      if (lineNumber > 0 && part instanceof ITextEditor){
        ITextEditor editor = (ITextEditor) part;
        IDocument doc = editor.getDocumentProvider().getDocument(new FileEditorInput(file));
        IRegion line = doc.getLineInformation(lineNumber - 1);
        editor.selectAndReveal(line.getOffset(), line.getLength());
      }
    } catch (PartInitException e) {//From opening the editor
      VJP.logError(e.getMessage(),e);   
    }catch (BadLocationException e) {//From a bad line number
      VJP.logError(e.getMessage(),e);
    }
    
  }

}
