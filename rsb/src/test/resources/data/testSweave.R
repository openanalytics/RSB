# Example Script to process a Sweave file
# 
# Author: Tobias Verbeke
###############################################################################

# process Sweave file in R
Sweave("testSweave.Rnw")

# compile .tex file into .pdf (using pdfLaTeX)
library(tools)
texi2dvi(file = "testSweave.tex", pdf = TRUE, clean = TRUE)

# remove some more files that should not be part of output zip file
figureFiles <- list.files(".", pattern = "testSweave-")
unlink(c("testSweave.tex", figureFiles))


