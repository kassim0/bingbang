import {Component, OnInit} from '@angular/core';
import {SearchBarComponent} from "../share/search-bar/search-bar.component";
import {ListGamesPopupComponent} from "../list-games-popup/list-games-popup.component";
import {MatDialog} from "@angular/material/dialog";
import {MatList, MatListItem} from "@angular/material/list";
import {MatDivider} from "@angular/material/divider";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    SearchBarComponent,
    ListGamesPopupComponent,
    MatListItem,
    MatDivider,
    MatList
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit{

  constructor(public dialog:MatDialog) {
  }

  ngOnInit(): void {
    this.OpenPopup();
  }

  OpenPopup() {
    this.dialog.open(ListGamesPopupComponent,{
      width: '40%',
      height:'90%',
      data: {}
    })
  }


}
