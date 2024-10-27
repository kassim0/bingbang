import {Component, Inject, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {MatButton, MatButtonModule} from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
  MatDialogTitle,
  MatDialogContent,
  MatDialogActions, MatDialogClose
} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {SearchBarComponent} from "../share/search-bar/search-bar.component";
import {JeuxRestControllerService, RawgResultsDto} from "../../openApi";
import {NgForOf} from "@angular/common";
import {MatList, MatListItem} from "@angular/material/list";
import {MatDivider} from "@angular/material/divider";
import {GameItemComponent} from "../share/game-item/game-item.component";

@Component({
  selector: 'app-list-games-popup',
  standalone: true,
  imports: [
    MatButton, MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose, SearchBarComponent, NgForOf, MatList, MatListItem, MatDivider, GameItemComponent,
  ],
  templateUrl: './list-games-popup.component.html',
  styleUrl: './list-games-popup.component.scss'
})
export class ListGamesPopupComponent{

  reponse : RawgResultsDto[] | undefined;

  @Input()
  gameNameSearch:string='';

  constructor(
    public dialogRef: MatDialogRef<ListGamesPopupComponent>,
    private jeuxRestControllerService: JeuxRestControllerService) {
  }

  close() {
    this.dialogRef.close();
  }

  receiveData(data:string){
    this.gameNameSearch=data;
    this.jeuxRestControllerService.getJeuxByName(this.gameNameSearch).subscribe((reponse)=>{
      this.reponse=reponse.results;
    })
  }
}
